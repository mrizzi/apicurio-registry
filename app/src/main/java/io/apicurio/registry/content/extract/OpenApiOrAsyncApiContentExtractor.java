/*
 * Copyright 2020 Red Hat
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.apicurio.registry.content.extract;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;

import io.apicurio.datamodels.Library;
import io.apicurio.datamodels.combined.visitors.CombinedVisitorAdapter;
import io.apicurio.datamodels.core.models.Document;
import io.apicurio.datamodels.core.models.common.Info;
import io.apicurio.datamodels.core.visitors.TraverserDirection;
import io.apicurio.registry.content.ContentHandle;

/**
 * Performs meta-data extraction for OpenAPI content.
 * @author eric.wittmann@gmail.com
 */
@ApplicationScoped
public class OpenApiOrAsyncApiContentExtractor implements ContentExtractor {

    @Inject
    Logger log;

    private OpenApiOrAsyncApiContentExtractor() {
    }

    @Override
    public ExtractedMetaData extract(ContentHandle content) {
        try {
            Document openApi = Library.readDocumentFromJSONString(content.content());
            MetaDataVisitor viz = new MetaDataVisitor();
            Library.visitTree(openApi, viz, TraverserDirection.down);

            ExtractedMetaData metaData = null;
            if (viz.name != null || viz.description != null) {
                metaData = new ExtractedMetaData();
            }
            if (viz.name != null) {
                metaData.setName(viz.name);
            }
            if (viz.description != null) {
                metaData.setDescription(viz.description);
            }
            return metaData;
        } catch (Exception e) {
            log.warn("Error extracting metadata from Open/Async API: {}", e.getMessage());
            return null;
        }
    }

    private static class MetaDataVisitor extends CombinedVisitorAdapter {

        String name;
        String description;

        /**
         * @see io.apicurio.datamodels.combined.visitors.CombinedVisitorAdapter#visitInfo(io.apicurio.datamodels.core.models.common.Info)
         */
        @Override
        public void visitInfo(Info node) {
            name = node.title;
            description = node.description;
        }

    }
}
