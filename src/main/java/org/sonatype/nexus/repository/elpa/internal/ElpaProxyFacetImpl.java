/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2018-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.elpa.internal;

import org.sonatype.nexus.repository.cache.CacheInfo;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.proxy.ProxyFacetSupport;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.Bucket;
import org.sonatype.nexus.repository.storage.Component;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.StorageTx;
import org.sonatype.nexus.repository.transaction.TransactionalStoreBlob;
import org.sonatype.nexus.repository.transaction.TransactionalTouchBlob;
import org.sonatype.nexus.repository.transaction.TransactionalTouchMetadata;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;
import org.sonatype.nexus.transaction.UnitOfWork;
import org.sonatype.nexus.repository.view.payloads.TempBlob;

import static org.joda.time.Duration.standardHours;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.repository.elpa.internal.AssetKind.ARCHIVE;
import static org.sonatype.nexus.repository.elpa.internal.AssetKind.SINGLE;
import static org.sonatype.nexus.repository.elpa.internal.AssetKind.SIGNATURE;
import static org.sonatype.nexus.repository.elpa.internal.AssetKind.CONTENTS;
import static org.sonatype.nexus.repository.storage.AssetEntityAdapter.P_ASSET_KIND;
import static org.sonatype.nexus.repository.elpa.internal.ElpaAttributes.P_DESCRIPTION;
import static org.sonatype.nexus.repository.elpa.internal.ElpaAttributes.P_URL;
import static org.sonatype.nexus.repository.elpa.internal.ElpaAttributes.P_KEYWORDS;

/**
 * ELPA {@link ProxyFacet}
 */
@Named
public class ElpaProxyFacetImpl
    extends ProxyFacetSupport
{
  private final ElpaParser elpaParser;
  private final ElpaPathUtils elpaPathUtils;
  private final ElpaDataAccess elpaDataAccess;

  @Inject
  public ElpaProxyFacetImpl(final ElpaParser elpaParser, final ElpaPathUtils elpaPathUtils, final ElpaDataAccess elpaDataAccess) {
    this.elpaParser = checkNotNull(elpaParser);
    this.elpaPathUtils = checkNotNull(elpaPathUtils);
    this.elpaDataAccess = checkNotNull(elpaDataAccess);
  }

  // HACK: Workaround for known CGLIB issue, forces an Import-Package for org.sonatype.nexus.repository.config
  @Override
  protected void doValidate(final Configuration configuration) throws Exception {
    super.doValidate(configuration);
  }

  @Override
  protected String getUrl(@Nonnull final Context context) {
    return context.getRequest().getPath().substring(1);
  }

  @Override
  protected Content store(final Context context, final Content content) throws IOException {
    AssetKind assetKind = context.getAttributes().require(AssetKind.class);
    TokenMatcher.State matcherState = elpaPathUtils.matcherState(context);
    switch (assetKind) {
      case ARCHIVE:
        return putArchive(elpaPathUtils.name(matcherState), elpaPathUtils.version(matcherState), elpaPathUtils.path(matcherState), content);
      case SINGLE:
        return putSingle(elpaPathUtils.name(matcherState), elpaPathUtils.version(matcherState), elpaPathUtils.path(matcherState), content);
      case SIGNATURE:
        return putSignature(elpaPathUtils.name(matcherState), elpaPathUtils.version(matcherState), elpaPathUtils.sigPath(matcherState), content);
      case CONTENTS:
        return putContents(content);
      default:
        throw new IllegalStateException();
    }
  }

  private Content putSingle(final String name, final String version, final String assetPath, final Content content) throws IOException {
    StorageFacet storageFacet = facet(StorageFacet.class);
    try (TempBlob tempBlob = storageFacet.createTempBlob(content.openInputStream(), ElpaDataAccess.HASH_ALGORITHMS)) {
      return doPutSingle(name, version, assetPath, tempBlob, content);
    }
  }

  @TransactionalStoreBlob
  protected Content doPutSingle(final String name,
                                final String version,
                                final String assetPath,
                                final TempBlob archiveContent,
                                final Payload payload) throws IOException
  {
    StorageTx tx = UnitOfWork.currentTx();
    Bucket bucket = tx.findBucket(getRepository());

    ElpaAttributes elpaAttributes = new ElpaAttributes();

    try (InputStream in = archiveContent.get()) {
      elpaAttributes = elpaParser.parseSingle(in);
    }

    Component component = elpaDataAccess.findComponent(tx, getRepository(), name, version);
    if (component == null) {
      component = tx.createComponent(bucket, getRepository().getFormat())
          .name(name)
          .version(version);
    }
    tx.saveComponent(component);

    Asset asset = elpaDataAccess.findAsset(tx, bucket, assetPath);
    if (asset == null) {
      asset = tx.createAsset(bucket, component);
      asset.name(assetPath);
      asset.formatAttributes().set(P_ASSET_KIND, SINGLE.name());
      asset.formatAttributes().set(P_DESCRIPTION, elpaAttributes.getDescription());
      asset.formatAttributes().set(P_URL, elpaAttributes.getUrl());
      asset.formatAttributes().set(P_KEYWORDS, String.join(", ", elpaAttributes.getKeywords()));
    }
    return elpaDataAccess.saveAsset(tx, asset, archiveContent, payload);
  }

  private Content putArchive(final String name, final String version, final String assetPath, final Content content) throws IOException {
    StorageFacet storageFacet = facet(StorageFacet.class);
    try (TempBlob tempBlob = storageFacet.createTempBlob(content.openInputStream(), ElpaDataAccess.HASH_ALGORITHMS)) {
      return doPutArchive(name, version, assetPath, tempBlob, content);
    }
  }

  @TransactionalStoreBlob
  protected Content doPutArchive(final String name,
                                 final String version,
                                 final String assetPath,
                                 final TempBlob archiveContent,
                                 final Payload payload) throws IOException
  {
    StorageTx tx = UnitOfWork.currentTx();
    Bucket bucket = tx.findBucket(getRepository());

    ElpaAttributes elpaAttributes = new ElpaAttributes();

    try (InputStream in = archiveContent.get()) {
      elpaAttributes = elpaParser.parseTar(name, in);
    }

    Component component = elpaDataAccess.findComponent(tx, getRepository(), name, version);
    if (component == null) {
      component = tx.createComponent(bucket, getRepository().getFormat())
          .name(name)
          .version(version);
    }
    tx.saveComponent(component);

    Asset asset = elpaDataAccess.findAsset(tx, bucket, assetPath);
    if (asset == null) {
      asset = tx.createAsset(bucket, component);
      asset.name(assetPath);
      asset.formatAttributes().set(P_ASSET_KIND, ARCHIVE.name());
      asset.formatAttributes().set(P_DESCRIPTION, elpaAttributes.getDescription());
      asset.formatAttributes().set(P_URL, elpaAttributes.getUrl());
      asset.formatAttributes().set(P_KEYWORDS, String.join(", ", elpaAttributes.getKeywords()));
    }
    return elpaDataAccess.saveAsset(tx, asset, archiveContent, payload);
  }

  private Content putSignature(final String name, final String version, final String assetPath, final Content content) throws IOException {
    StorageFacet storageFacet = facet(StorageFacet.class);
    try (TempBlob tempBlob = storageFacet.createTempBlob(content.openInputStream(), ElpaDataAccess.HASH_ALGORITHMS)) {
      return doPutSignature(name, version, assetPath, tempBlob, content);
    }
  }

  @TransactionalStoreBlob
  protected Content doPutSignature(final String name,
                                   final String version,
                                   final String assetPath,
                                   final TempBlob archiveContent,
                                   final Payload payload) throws IOException
  {
    StorageTx tx = UnitOfWork.currentTx();
    Bucket bucket = tx.findBucket(getRepository());

    Component component = elpaDataAccess.findComponent(tx, getRepository(), name, version);
    if (component == null) {
      component = tx.createComponent(bucket, getRepository().getFormat())
          .name(name)
          .version(version);
    }
    tx.saveComponent(component);

    Asset asset = elpaDataAccess.findAsset(tx, bucket, assetPath);
    if (asset == null) {
      asset = tx.createAsset(bucket, component);
      asset.name(assetPath);
      asset.formatAttributes().set(P_ASSET_KIND, SIGNATURE.name());
    }
    return elpaDataAccess.saveAsset(tx, asset, archiveContent, payload);
  }

  private Content putContents(final Content content) throws IOException {
    StorageFacet storageFacet = facet(StorageFacet.class);
    try (TempBlob tempBlob = storageFacet.createTempBlob(content.openInputStream(), ElpaDataAccess.HASH_ALGORITHMS)) {
      return doPutContents(tempBlob, content);
    }
  }

  @TransactionalStoreBlob
  protected Content doPutContents(final TempBlob metadataContent,
                                  final Payload payload) throws IOException
  {
    StorageTx tx = UnitOfWork.currentTx();
    Bucket bucket = tx.findBucket(getRepository());

    String assetPath = elpaPathUtils.contentsPath();

    Asset asset = elpaDataAccess.findAsset(tx, bucket, assetPath);
    if (asset == null) {
      asset = tx.createAsset(bucket, getRepository().getFormat());
      asset.name(assetPath);
      asset.formatAttributes().set(P_ASSET_KIND, CONTENTS.name());
    }
    return elpaDataAccess.saveAsset(tx, asset, metadataContent, payload);
  }

  @Override
  protected void indicateVerified(final Context context, final Content content, final CacheInfo cacheInfo)
      throws IOException
  {
    setCacheInfo(content, cacheInfo);
  }

  @TransactionalTouchMetadata
  public void setCacheInfo(final Content content, final CacheInfo cacheInfo) throws IOException {
    StorageTx tx = UnitOfWork.currentTx();
    Asset asset = Content.findAsset(tx, tx.findBucket(getRepository()), content);
    if (asset == null) {
      log.debug(
          "Attempting to set cache info for non-existent ELPA asset {}", content.getAttributes().require(Asset.class)
      );
      return;
    }
    log.debug("Updating cacheInfo of {} to {}", asset, cacheInfo);
    CacheInfo.applyToAsset(asset, cacheInfo);
    tx.saveAsset(asset);
  }

  @Nullable
  @Override
  protected Content getCachedContent(final Context context) throws IOException {
    AssetKind assetKind = context.getAttributes().require(AssetKind.class);
    TokenMatcher.State matcherState = elpaPathUtils.matcherState(context);
    switch (assetKind) {
      case ARCHIVE:
      case SINGLE:
        return getAsset(elpaPathUtils.path(matcherState));
      case SIGNATURE:
        return getAsset(elpaPathUtils.sigPath(matcherState));
      case CONTENTS:
        return getAsset(elpaPathUtils.contentsPath());
      default:
        throw new IllegalStateException();
    }
  }

  @TransactionalTouchBlob
  protected Content getAsset(final String name) {
    StorageTx tx = UnitOfWork.currentTx();
    Asset asset = elpaDataAccess.findAsset(tx, tx.findBucket(getRepository()), name);
    if (asset == null) {
      return null;
    }
    if (asset.markAsDownloaded(standardHours(0))) {
      tx.saveAsset(asset);
    }
    return elpaDataAccess.toContent(asset, tx.requireBlob(asset.requireBlobRef()));
  }
}
