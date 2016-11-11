begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.percolator
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|percolator
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|analysis
operator|.
name|Analyzer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|analysis
operator|.
name|core
operator|.
name|WhitespaceAnalyzer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|BooleanClause
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|BooleanQuery
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|IndexSearcher
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|MatchAllDocsQuery
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|Query
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ResourceNotFoundException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|mapping
operator|.
name|put
operator|.
name|PutMappingRequest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|get
operator|.
name|GetRequest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|get
operator|.
name|GetResponse
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
name|BytesArray
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
name|compress
operator|.
name|CompressedXContent
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
name|index
operator|.
name|get
operator|.
name|GetResult
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|MapperService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|ParseContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|ParsedDocument
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|RandomDocumentPicks
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
operator|.
name|Plugin
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|internal
operator|.
name|SearchContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|AbstractQueryTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|equalTo
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|sameInstance
import|;
end_import

begin_class
DECL|class|PercolateQueryBuilderTests
specifier|public
class|class
name|PercolateQueryBuilderTests
extends|extends
name|AbstractQueryTestCase
argument_list|<
name|PercolateQueryBuilder
argument_list|>
block|{
DECL|field|SHUFFLE_PROTECTED_FIELDS
specifier|private
specifier|static
specifier|final
name|String
index|[]
name|SHUFFLE_PROTECTED_FIELDS
init|=
operator|new
name|String
index|[]
block|{
name|PercolateQueryBuilder
operator|.
name|DOCUMENT_FIELD
operator|.
name|getPreferredName
argument_list|()
block|}
decl_stmt|;
DECL|field|queryField
specifier|private
specifier|static
name|String
name|queryField
decl_stmt|;
DECL|field|docType
specifier|private
specifier|static
name|String
name|docType
decl_stmt|;
DECL|field|indexedDocumentIndex
specifier|private
name|String
name|indexedDocumentIndex
decl_stmt|;
DECL|field|indexedDocumentType
specifier|private
name|String
name|indexedDocumentType
decl_stmt|;
DECL|field|indexedDocumentId
specifier|private
name|String
name|indexedDocumentId
decl_stmt|;
DECL|field|indexedDocumentRouting
specifier|private
name|String
name|indexedDocumentRouting
decl_stmt|;
DECL|field|indexedDocumentPreference
specifier|private
name|String
name|indexedDocumentPreference
decl_stmt|;
DECL|field|indexedDocumentVersion
specifier|private
name|Long
name|indexedDocumentVersion
decl_stmt|;
DECL|field|documentSource
specifier|private
name|BytesReference
name|documentSource
decl_stmt|;
DECL|field|indexedDocumentExists
specifier|private
name|boolean
name|indexedDocumentExists
init|=
literal|true
decl_stmt|;
annotation|@
name|Override
DECL|method|getPlugins
specifier|protected
name|Collection
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Plugin
argument_list|>
argument_list|>
name|getPlugins
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|singleton
argument_list|(
name|PercolatorPlugin
operator|.
name|class
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|initializeAdditionalMappings
specifier|protected
name|void
name|initializeAdditionalMappings
parameter_list|(
name|MapperService
name|mapperService
parameter_list|)
throws|throws
name|IOException
block|{
name|queryField
operator|=
name|randomAsciiOfLength
argument_list|(
literal|4
argument_list|)
expr_stmt|;
name|docType
operator|=
name|randomAsciiOfLength
argument_list|(
literal|4
argument_list|)
expr_stmt|;
name|mapperService
operator|.
name|merge
argument_list|(
literal|"query_type"
argument_list|,
operator|new
name|CompressedXContent
argument_list|(
name|PutMappingRequest
operator|.
name|buildFromSimplifiedDef
argument_list|(
literal|"query_type"
argument_list|,
name|queryField
argument_list|,
literal|"type=percolator"
argument_list|)
operator|.
name|string
argument_list|()
argument_list|)
argument_list|,
name|MapperService
operator|.
name|MergeReason
operator|.
name|MAPPING_UPDATE
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|mapperService
operator|.
name|merge
argument_list|(
name|docType
argument_list|,
operator|new
name|CompressedXContent
argument_list|(
name|PutMappingRequest
operator|.
name|buildFromSimplifiedDef
argument_list|(
name|docType
argument_list|,
name|STRING_FIELD_NAME
argument_list|,
literal|"type=text"
argument_list|)
operator|.
name|string
argument_list|()
argument_list|)
argument_list|,
name|MapperService
operator|.
name|MergeReason
operator|.
name|MAPPING_UPDATE
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doCreateTestQueryBuilder
specifier|protected
name|PercolateQueryBuilder
name|doCreateTestQueryBuilder
parameter_list|()
block|{
return|return
name|doCreateTestQueryBuilder
argument_list|(
name|randomBoolean
argument_list|()
argument_list|)
return|;
block|}
DECL|method|doCreateTestQueryBuilder
specifier|private
name|PercolateQueryBuilder
name|doCreateTestQueryBuilder
parameter_list|(
name|boolean
name|indexedDocument
parameter_list|)
block|{
name|documentSource
operator|=
name|randomSource
argument_list|()
expr_stmt|;
if|if
condition|(
name|indexedDocument
condition|)
block|{
name|indexedDocumentIndex
operator|=
name|randomAsciiOfLength
argument_list|(
literal|4
argument_list|)
expr_stmt|;
name|indexedDocumentType
operator|=
name|randomAsciiOfLength
argument_list|(
literal|4
argument_list|)
expr_stmt|;
name|indexedDocumentId
operator|=
name|randomAsciiOfLength
argument_list|(
literal|4
argument_list|)
expr_stmt|;
name|indexedDocumentRouting
operator|=
name|randomAsciiOfLength
argument_list|(
literal|4
argument_list|)
expr_stmt|;
name|indexedDocumentPreference
operator|=
name|randomAsciiOfLength
argument_list|(
literal|4
argument_list|)
expr_stmt|;
name|indexedDocumentVersion
operator|=
operator|(
name|long
operator|)
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
return|return
operator|new
name|PercolateQueryBuilder
argument_list|(
name|queryField
argument_list|,
name|docType
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
return|;
block|}
else|else
block|{
return|return
operator|new
name|PercolateQueryBuilder
argument_list|(
name|queryField
argument_list|,
name|docType
argument_list|,
name|documentSource
argument_list|)
return|;
block|}
block|}
comment|/**      * we don't want to shuffle the "document" field internally in {@link #testFromXContent()} because even though the      * documents would be functionally the same, their {@link BytesReference} representation isn't and thats what we      * compare when check for equality of the original and the shuffled builder      */
annotation|@
name|Override
DECL|method|shuffleProtectedFields
specifier|protected
name|String
index|[]
name|shuffleProtectedFields
parameter_list|()
block|{
return|return
name|SHUFFLE_PROTECTED_FIELDS
return|;
block|}
annotation|@
name|Override
DECL|method|executeGet
specifier|protected
name|GetResponse
name|executeGet
parameter_list|(
name|GetRequest
name|getRequest
parameter_list|)
block|{
name|assertThat
argument_list|(
name|getRequest
operator|.
name|index
argument_list|()
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
name|indexedDocumentIndex
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|getRequest
operator|.
name|type
argument_list|()
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
name|indexedDocumentType
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|getRequest
operator|.
name|id
argument_list|()
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
name|indexedDocumentId
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|getRequest
operator|.
name|routing
argument_list|()
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
name|indexedDocumentRouting
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|getRequest
operator|.
name|preference
argument_list|()
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
name|indexedDocumentPreference
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|getRequest
operator|.
name|version
argument_list|()
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
name|indexedDocumentVersion
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|indexedDocumentExists
condition|)
block|{
return|return
operator|new
name|GetResponse
argument_list|(
operator|new
name|GetResult
argument_list|(
name|indexedDocumentIndex
argument_list|,
name|indexedDocumentType
argument_list|,
name|indexedDocumentId
argument_list|,
literal|0L
argument_list|,
literal|true
argument_list|,
name|documentSource
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|GetResponse
argument_list|(
operator|new
name|GetResult
argument_list|(
name|indexedDocumentIndex
argument_list|,
name|indexedDocumentType
argument_list|,
name|indexedDocumentId
argument_list|,
operator|-
literal|1
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|doAssertLuceneQuery
specifier|protected
name|void
name|doAssertLuceneQuery
parameter_list|(
name|PercolateQueryBuilder
name|queryBuilder
parameter_list|,
name|Query
name|query
parameter_list|,
name|SearchContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|assertThat
argument_list|(
name|query
argument_list|,
name|Matchers
operator|.
name|instanceOf
argument_list|(
name|PercolateQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|PercolateQuery
name|percolateQuery
init|=
operator|(
name|PercolateQuery
operator|)
name|query
decl_stmt|;
name|assertThat
argument_list|(
name|percolateQuery
operator|.
name|getDocumentType
argument_list|()
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
name|queryBuilder
operator|.
name|getDocumentType
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateQuery
operator|.
name|getDocumentSource
argument_list|()
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
name|documentSource
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|testMustRewrite
specifier|public
name|void
name|testMustRewrite
parameter_list|()
throws|throws
name|IOException
block|{
name|PercolateQueryBuilder
name|pqb
init|=
name|doCreateTestQueryBuilder
argument_list|(
literal|true
argument_list|)
decl_stmt|;
name|IllegalStateException
name|e
init|=
name|expectThrows
argument_list|(
name|IllegalStateException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|pqb
operator|.
name|toQuery
argument_list|(
name|createShardContext
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"query builder must be rewritten first"
argument_list|)
argument_list|)
expr_stmt|;
name|QueryBuilder
name|rewrite
init|=
name|pqb
operator|.
name|rewrite
argument_list|(
name|createShardContext
argument_list|()
argument_list|)
decl_stmt|;
name|PercolateQueryBuilder
name|geoShapeQueryBuilder
init|=
operator|new
name|PercolateQueryBuilder
argument_list|(
name|pqb
operator|.
name|getField
argument_list|()
argument_list|,
name|pqb
operator|.
name|getDocumentType
argument_list|()
argument_list|,
name|documentSource
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|geoShapeQueryBuilder
argument_list|,
name|rewrite
argument_list|)
expr_stmt|;
block|}
DECL|method|testIndexedDocumentDoesNotExist
specifier|public
name|void
name|testIndexedDocumentDoesNotExist
parameter_list|()
throws|throws
name|IOException
block|{
name|indexedDocumentExists
operator|=
literal|false
expr_stmt|;
name|PercolateQueryBuilder
name|pqb
init|=
name|doCreateTestQueryBuilder
argument_list|(
literal|true
argument_list|)
decl_stmt|;
name|ResourceNotFoundException
name|e
init|=
name|expectThrows
argument_list|(
name|ResourceNotFoundException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|pqb
operator|.
name|rewrite
argument_list|(
name|createShardContext
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|expectedString
init|=
literal|"indexed document ["
operator|+
name|indexedDocumentIndex
operator|+
literal|"/"
operator|+
name|indexedDocumentType
operator|+
literal|"/"
operator|+
name|indexedDocumentId
operator|+
literal|"] couldn't be found"
decl_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|expectedString
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getObjectsHoldingArbitraryContent
specifier|protected
name|Set
argument_list|<
name|String
argument_list|>
name|getObjectsHoldingArbitraryContent
parameter_list|()
block|{
comment|//document contains arbitrary content, no error expected when an object is added to it
return|return
name|Collections
operator|.
name|singleton
argument_list|(
name|PercolateQueryBuilder
operator|.
name|DOCUMENT_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|)
return|;
block|}
DECL|method|testRequiredParameters
specifier|public
name|void
name|testRequiredParameters
parameter_list|()
block|{
name|IllegalArgumentException
name|e
init|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
block|{
operator|new
name|PercolateQueryBuilder
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
operator|new
name|BytesArray
argument_list|(
literal|"{}"
argument_list|)
argument_list|)
expr_stmt|;
block|}
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"[field] is a required argument"
argument_list|)
argument_list|)
expr_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
operator|new
name|PercolateQueryBuilder
argument_list|(
literal|"_field"
argument_list|,
literal|null
argument_list|,
operator|new
name|BytesArray
argument_list|(
literal|"{}"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"[document_type] is a required argument"
argument_list|)
argument_list|)
expr_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
operator|new
name|PercolateQueryBuilder
argument_list|(
literal|"_field"
argument_list|,
literal|"_document_type"
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"[document] is a required argument"
argument_list|)
argument_list|)
expr_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
block|{
operator|new
name|PercolateQueryBuilder
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|"_index"
argument_list|,
literal|"_type"
argument_list|,
literal|"_id"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"[field] is a required argument"
argument_list|)
argument_list|)
expr_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
block|{
operator|new
name|PercolateQueryBuilder
argument_list|(
literal|"_field"
argument_list|,
literal|null
argument_list|,
literal|"_index"
argument_list|,
literal|"_type"
argument_list|,
literal|"_id"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"[document_type] is a required argument"
argument_list|)
argument_list|)
expr_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
block|{
operator|new
name|PercolateQueryBuilder
argument_list|(
literal|"_field"
argument_list|,
literal|"_document_type"
argument_list|,
literal|null
argument_list|,
literal|"_type"
argument_list|,
literal|"_id"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"[index] is a required argument"
argument_list|)
argument_list|)
expr_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
block|{
operator|new
name|PercolateQueryBuilder
argument_list|(
literal|"_field"
argument_list|,
literal|"_document_type"
argument_list|,
literal|"_index"
argument_list|,
literal|null
argument_list|,
literal|"_id"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"[type] is a required argument"
argument_list|)
argument_list|)
expr_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
block|{
operator|new
name|PercolateQueryBuilder
argument_list|(
literal|"_field"
argument_list|,
literal|"_document_type"
argument_list|,
literal|"_index"
argument_list|,
literal|"_type"
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"[id] is a required argument"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testFromJsonNoDocumentType
specifier|public
name|void
name|testFromJsonNoDocumentType
parameter_list|()
throws|throws
name|IOException
block|{
name|IllegalArgumentException
name|e
init|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|parseQuery
argument_list|(
literal|"{\"percolate\" : { \"document\": {}}"
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"[percolate] query is missing required [document_type] parameter"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testCreateMultiDocumentSearcher
specifier|public
name|void
name|testCreateMultiDocumentSearcher
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|numDocs
init|=
name|randomIntBetween
argument_list|(
literal|2
argument_list|,
literal|8
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ParseContext
operator|.
name|Document
argument_list|>
name|docs
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|numDocs
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numDocs
condition|;
name|i
operator|++
control|)
block|{
name|docs
operator|.
name|add
argument_list|(
operator|new
name|ParseContext
operator|.
name|Document
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Analyzer
name|analyzer
init|=
operator|new
name|WhitespaceAnalyzer
argument_list|()
decl_stmt|;
name|ParsedDocument
name|parsedDocument
init|=
operator|new
name|ParsedDocument
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
literal|"_id"
argument_list|,
literal|"_type"
argument_list|,
literal|null
argument_list|,
operator|-
literal|1L
argument_list|,
operator|-
literal|1L
argument_list|,
name|docs
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|IndexSearcher
name|indexSearcher
init|=
name|PercolateQueryBuilder
operator|.
name|createMultiDocumentSearcher
argument_list|(
name|analyzer
argument_list|,
name|parsedDocument
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|indexSearcher
operator|.
name|getIndexReader
argument_list|()
operator|.
name|numDocs
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|numDocs
argument_list|)
argument_list|)
expr_stmt|;
comment|// ensure that any query get modified so that the nested docs are never included as hits:
name|Query
name|query
init|=
operator|new
name|MatchAllDocsQuery
argument_list|()
decl_stmt|;
name|BooleanQuery
name|result
init|=
operator|(
name|BooleanQuery
operator|)
name|indexSearcher
operator|.
name|createNormalizedWeight
argument_list|(
name|query
argument_list|,
literal|true
argument_list|)
operator|.
name|getQuery
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|clauses
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|clauses
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getQuery
argument_list|()
argument_list|,
name|sameInstance
argument_list|(
name|query
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|clauses
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getOccur
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|BooleanClause
operator|.
name|Occur
operator|.
name|MUST
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|clauses
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getOccur
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|BooleanClause
operator|.
name|Occur
operator|.
name|MUST_NOT
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|randomSource
specifier|private
specifier|static
name|BytesReference
name|randomSource
parameter_list|()
block|{
try|try
block|{
name|XContentBuilder
name|xContent
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
decl_stmt|;
name|xContent
operator|.
name|map
argument_list|(
name|RandomDocumentPicks
operator|.
name|randomSource
argument_list|(
name|random
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|xContent
operator|.
name|bytes
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|isCachable
specifier|protected
name|boolean
name|isCachable
parameter_list|(
name|PercolateQueryBuilder
name|queryBuilder
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

