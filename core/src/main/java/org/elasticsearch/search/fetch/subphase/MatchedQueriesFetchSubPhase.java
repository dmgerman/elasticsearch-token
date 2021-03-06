begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.fetch.subphase
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|fetch
operator|.
name|subphase
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
name|index
operator|.
name|IndexReader
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
name|index
operator|.
name|LeafReaderContext
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
name|index
operator|.
name|ReaderUtil
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
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|ScorerSupplier
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
name|Weight
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
name|util
operator|.
name|Bits
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ExceptionsHelper
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
name|lucene
operator|.
name|Lucene
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
name|fetch
operator|.
name|FetchSubPhase
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
name|SearchHit
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
name|search
operator|.
name|internal
operator|.
name|SearchContext
operator|.
name|Lifetime
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|Map
import|;
end_import

begin_class
DECL|class|MatchedQueriesFetchSubPhase
specifier|public
specifier|final
class|class
name|MatchedQueriesFetchSubPhase
implements|implements
name|FetchSubPhase
block|{
annotation|@
name|Override
DECL|method|hitsExecute
specifier|public
name|void
name|hitsExecute
parameter_list|(
name|SearchContext
name|context
parameter_list|,
name|SearchHit
index|[]
name|hits
parameter_list|)
block|{
if|if
condition|(
name|hits
operator|.
name|length
operator|==
literal|0
operator|||
comment|// in case the request has only suggest, parsed query is null
name|context
operator|.
name|parsedQuery
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|hits
operator|=
name|hits
operator|.
name|clone
argument_list|()
expr_stmt|;
comment|// don't modify the incoming hits
name|Arrays
operator|.
name|sort
argument_list|(
name|hits
argument_list|,
parameter_list|(
name|a
parameter_list|,
name|b
parameter_list|)
lambda|->
name|Integer
operator|.
name|compare
argument_list|(
name|a
operator|.
name|docId
argument_list|()
argument_list|,
name|b
operator|.
name|docId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|List
argument_list|<
name|String
argument_list|>
index|[]
name|matchedQueries
init|=
operator|new
name|List
index|[
name|hits
operator|.
name|length
index|]
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
name|matchedQueries
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|matchedQueries
index|[
name|i
index|]
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|Query
argument_list|>
name|namedQueries
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|context
operator|.
name|parsedQuery
argument_list|()
operator|.
name|namedFilters
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|context
operator|.
name|parsedPostFilter
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|namedQueries
operator|.
name|putAll
argument_list|(
name|context
operator|.
name|parsedPostFilter
argument_list|()
operator|.
name|namedFilters
argument_list|()
argument_list|)
expr_stmt|;
block|}
try|try
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Query
argument_list|>
name|entry
range|:
name|namedQueries
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|name
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|Query
name|query
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|int
name|readerIndex
init|=
operator|-
literal|1
decl_stmt|;
name|int
name|docBase
init|=
operator|-
literal|1
decl_stmt|;
name|Weight
name|weight
init|=
name|context
operator|.
name|searcher
argument_list|()
operator|.
name|createNormalizedWeight
argument_list|(
name|query
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Bits
name|matchingDocs
init|=
literal|null
decl_stmt|;
specifier|final
name|IndexReader
name|indexReader
init|=
name|context
operator|.
name|searcher
argument_list|()
operator|.
name|getIndexReader
argument_list|()
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
name|hits
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|SearchHit
name|hit
init|=
name|hits
index|[
name|i
index|]
decl_stmt|;
name|int
name|hitReaderIndex
init|=
name|ReaderUtil
operator|.
name|subIndex
argument_list|(
name|hit
operator|.
name|docId
argument_list|()
argument_list|,
name|indexReader
operator|.
name|leaves
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|readerIndex
operator|!=
name|hitReaderIndex
condition|)
block|{
name|readerIndex
operator|=
name|hitReaderIndex
expr_stmt|;
name|LeafReaderContext
name|ctx
init|=
name|indexReader
operator|.
name|leaves
argument_list|()
operator|.
name|get
argument_list|(
name|readerIndex
argument_list|)
decl_stmt|;
name|docBase
operator|=
name|ctx
operator|.
name|docBase
expr_stmt|;
comment|// scorers can be costly to create, so reuse them across docs of the same segment
name|ScorerSupplier
name|scorerSupplier
init|=
name|weight
operator|.
name|scorerSupplier
argument_list|(
name|ctx
argument_list|)
decl_stmt|;
name|matchingDocs
operator|=
name|Lucene
operator|.
name|asSequentialAccessBits
argument_list|(
name|ctx
operator|.
name|reader
argument_list|()
operator|.
name|maxDoc
argument_list|()
argument_list|,
name|scorerSupplier
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|matchingDocs
operator|.
name|get
argument_list|(
name|hit
operator|.
name|docId
argument_list|()
operator|-
name|docBase
argument_list|)
condition|)
block|{
name|matchedQueries
index|[
name|i
index|]
operator|.
name|add
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
block|}
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|hits
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|hits
index|[
name|i
index|]
operator|.
name|matchedQueries
argument_list|(
name|matchedQueries
index|[
name|i
index|]
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|matchedQueries
index|[
name|i
index|]
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
name|ExceptionsHelper
operator|.
name|convertToElastic
argument_list|(
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|context
operator|.
name|clearReleasables
argument_list|(
name|Lifetime
operator|.
name|COLLECTION
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

