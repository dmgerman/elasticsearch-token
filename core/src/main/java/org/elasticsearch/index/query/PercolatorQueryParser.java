begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.query
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|ParseField
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|ParsingException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|bytes
operator|.
name|BytesReference
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentParser
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_class
DECL|class|PercolatorQueryParser
specifier|public
class|class
name|PercolatorQueryParser
implements|implements
name|QueryParser
argument_list|<
name|PercolatorQueryBuilder
argument_list|>
block|{
DECL|field|QUERY_NAME_FIELD
specifier|public
specifier|static
specifier|final
name|ParseField
name|QUERY_NAME_FIELD
init|=
operator|new
name|ParseField
argument_list|(
name|PercolatorQueryBuilder
operator|.
name|NAME
argument_list|)
decl_stmt|;
DECL|field|DOCUMENT_FIELD
specifier|public
specifier|static
specifier|final
name|ParseField
name|DOCUMENT_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"document"
argument_list|)
decl_stmt|;
DECL|field|DOCUMENT_TYPE_FIELD
specifier|public
specifier|static
specifier|final
name|ParseField
name|DOCUMENT_TYPE_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"document_type"
argument_list|)
decl_stmt|;
DECL|field|INDEXED_DOCUMENT_FIELD_INDEX
specifier|public
specifier|static
specifier|final
name|ParseField
name|INDEXED_DOCUMENT_FIELD_INDEX
init|=
operator|new
name|ParseField
argument_list|(
literal|"index"
argument_list|)
decl_stmt|;
DECL|field|INDEXED_DOCUMENT_FIELD_TYPE
specifier|public
specifier|static
specifier|final
name|ParseField
name|INDEXED_DOCUMENT_FIELD_TYPE
init|=
operator|new
name|ParseField
argument_list|(
literal|"type"
argument_list|)
decl_stmt|;
DECL|field|INDEXED_DOCUMENT_FIELD_ID
specifier|public
specifier|static
specifier|final
name|ParseField
name|INDEXED_DOCUMENT_FIELD_ID
init|=
operator|new
name|ParseField
argument_list|(
literal|"id"
argument_list|)
decl_stmt|;
DECL|field|INDEXED_DOCUMENT_FIELD_ROUTING
specifier|public
specifier|static
specifier|final
name|ParseField
name|INDEXED_DOCUMENT_FIELD_ROUTING
init|=
operator|new
name|ParseField
argument_list|(
literal|"routing"
argument_list|)
decl_stmt|;
DECL|field|INDEXED_DOCUMENT_FIELD_PREFERENCE
specifier|public
specifier|static
specifier|final
name|ParseField
name|INDEXED_DOCUMENT_FIELD_PREFERENCE
init|=
operator|new
name|ParseField
argument_list|(
literal|"preference"
argument_list|)
decl_stmt|;
DECL|field|INDEXED_DOCUMENT_FIELD_VERSION
specifier|public
specifier|static
specifier|final
name|ParseField
name|INDEXED_DOCUMENT_FIELD_VERSION
init|=
operator|new
name|ParseField
argument_list|(
literal|"version"
argument_list|)
decl_stmt|;
annotation|@
name|Override
DECL|method|fromXContent
specifier|public
name|PercolatorQueryBuilder
name|fromXContent
parameter_list|(
name|QueryParseContext
name|parseContext
parameter_list|)
throws|throws
name|IOException
block|{
name|XContentParser
name|parser
init|=
name|parseContext
operator|.
name|parser
argument_list|()
decl_stmt|;
name|float
name|boost
init|=
name|AbstractQueryBuilder
operator|.
name|DEFAULT_BOOST
decl_stmt|;
name|String
name|documentType
init|=
literal|null
decl_stmt|;
name|String
name|indexedDocumentIndex
init|=
literal|null
decl_stmt|;
name|String
name|indexedDocumentType
init|=
literal|null
decl_stmt|;
name|String
name|indexedDocumentId
init|=
literal|null
decl_stmt|;
name|String
name|indexedDocumentRouting
init|=
literal|null
decl_stmt|;
name|String
name|indexedDocumentPreference
init|=
literal|null
decl_stmt|;
name|Long
name|indexedDocumentVersion
init|=
literal|null
decl_stmt|;
name|BytesReference
name|source
init|=
literal|null
decl_stmt|;
name|String
name|queryName
init|=
literal|null
decl_stmt|;
name|String
name|currentFieldName
init|=
literal|null
decl_stmt|;
name|XContentParser
operator|.
name|Token
name|token
decl_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
name|currentFieldName
operator|=
name|parser
operator|.
name|currentName
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
if|if
condition|(
name|parseContext
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|DOCUMENT_FIELD
argument_list|)
condition|)
block|{
try|try
init|(
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
init|)
block|{
name|builder
operator|.
name|copyCurrentStructure
argument_list|(
name|parser
argument_list|)
expr_stmt|;
name|builder
operator|.
name|flush
argument_list|()
expr_stmt|;
name|source
operator|=
name|builder
operator|.
name|bytes
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"["
operator|+
name|PercolatorQueryBuilder
operator|.
name|NAME
operator|+
literal|"] query does not support ["
operator|+
name|token
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|token
operator|.
name|isValue
argument_list|()
condition|)
block|{
if|if
condition|(
name|parseContext
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|DOCUMENT_TYPE_FIELD
argument_list|)
condition|)
block|{
name|documentType
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parseContext
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|INDEXED_DOCUMENT_FIELD_INDEX
argument_list|)
condition|)
block|{
name|indexedDocumentIndex
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parseContext
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|INDEXED_DOCUMENT_FIELD_TYPE
argument_list|)
condition|)
block|{
name|indexedDocumentType
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parseContext
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|INDEXED_DOCUMENT_FIELD_ID
argument_list|)
condition|)
block|{
name|indexedDocumentId
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parseContext
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|INDEXED_DOCUMENT_FIELD_ROUTING
argument_list|)
condition|)
block|{
name|indexedDocumentRouting
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parseContext
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|INDEXED_DOCUMENT_FIELD_PREFERENCE
argument_list|)
condition|)
block|{
name|indexedDocumentPreference
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parseContext
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|INDEXED_DOCUMENT_FIELD_VERSION
argument_list|)
condition|)
block|{
name|indexedDocumentVersion
operator|=
name|parser
operator|.
name|longValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parseContext
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|AbstractQueryBuilder
operator|.
name|BOOST_FIELD
argument_list|)
condition|)
block|{
name|boost
operator|=
name|parser
operator|.
name|floatValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parseContext
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|AbstractQueryBuilder
operator|.
name|NAME_FIELD
argument_list|)
condition|)
block|{
name|queryName
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"["
operator|+
name|PercolatorQueryBuilder
operator|.
name|NAME
operator|+
literal|"] query does not support ["
operator|+
name|currentFieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"["
operator|+
name|PercolatorQueryBuilder
operator|.
name|NAME
operator|+
literal|"] query does not support ["
operator|+
name|token
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|documentType
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"["
operator|+
name|PercolatorQueryBuilder
operator|.
name|NAME
operator|+
literal|"] query is missing required ["
operator|+
name|DOCUMENT_TYPE_FIELD
operator|.
name|getPreferredName
argument_list|()
operator|+
literal|"] parameter"
argument_list|)
throw|;
block|}
name|PercolatorQueryBuilder
name|queryBuilder
decl_stmt|;
if|if
condition|(
name|source
operator|!=
literal|null
condition|)
block|{
name|queryBuilder
operator|=
operator|new
name|PercolatorQueryBuilder
argument_list|(
name|documentType
argument_list|,
name|source
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|indexedDocumentId
operator|!=
literal|null
condition|)
block|{
name|queryBuilder
operator|=
operator|new
name|PercolatorQueryBuilder
argument_list|(
name|documentType
argument_list|,
name|indexedDocumentIndex
argument_list|,
name|indexedDocumentType
argument_list|,
name|indexedDocumentId
argument_list|,
name|indexedDocumentRouting
argument_list|,
name|indexedDocumentPreference
argument_list|,
name|indexedDocumentVersion
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"["
operator|+
name|PercolatorQueryBuilder
operator|.
name|NAME
operator|+
literal|"] query, nothing to percolate"
argument_list|)
throw|;
block|}
name|queryBuilder
operator|.
name|queryName
argument_list|(
name|queryName
argument_list|)
expr_stmt|;
name|queryBuilder
operator|.
name|boost
argument_list|(
name|boost
argument_list|)
expr_stmt|;
return|return
name|queryBuilder
return|;
block|}
annotation|@
name|Override
DECL|method|getBuilderPrototype
specifier|public
name|PercolatorQueryBuilder
name|getBuilderPrototype
parameter_list|()
block|{
return|return
name|PercolatorQueryBuilder
operator|.
name|PROTO
return|;
block|}
block|}
end_class

end_unit

