begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.shard
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|shard
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
name|DirectoryReader
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
name|FilterDirectoryReader
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
name|LeafReader
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
name|elasticsearch
operator|.
name|ElasticsearchException
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
name|index
operator|.
name|ElasticsearchDirectoryReader
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
name|engine
operator|.
name|Engine
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
name|engine
operator|.
name|EngineConfig
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

begin_comment
comment|/**  * Extension point to add custom functionality at request time to the {@link DirectoryReader}  * and {@link IndexSearcher} managed by the {@link Engine}.  */
end_comment

begin_class
DECL|class|IndexSearcherWrapper
specifier|public
class|class
name|IndexSearcherWrapper
block|{
comment|/**      * @param reader The provided directory reader to be wrapped to add custom functionality      * @return a new directory reader wrapping the provided directory reader or if no wrapping was performed      *         the provided directory reader      */
DECL|method|wrap
specifier|protected
name|DirectoryReader
name|wrap
parameter_list|(
name|DirectoryReader
name|reader
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|reader
return|;
block|}
comment|/**      * @param engineConfig  The engine config which can be used to get the query cache and query cache policy from      *                      when creating a new index searcher      * @param searcher      The provided index searcher to be wrapped to add custom functionality      * @return a new index searcher wrapping the provided index searcher or if no wrapping was performed      *         the provided index searcher      */
DECL|method|wrap
specifier|protected
name|IndexSearcher
name|wrap
parameter_list|(
name|EngineConfig
name|engineConfig
parameter_list|,
name|IndexSearcher
name|searcher
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|searcher
return|;
block|}
comment|/**      * If there are configured {@link IndexSearcherWrapper} instances, the {@link IndexSearcher} of the provided engine searcher      * gets wrapped and a new {@link Engine.Searcher} instances is returned, otherwise the provided {@link Engine.Searcher} is returned.      *      * This is invoked each time a {@link Engine.Searcher} is requested to do an operation. (for example search)      */
DECL|method|wrap
specifier|public
specifier|final
name|Engine
operator|.
name|Searcher
name|wrap
parameter_list|(
name|EngineConfig
name|engineConfig
parameter_list|,
name|Engine
operator|.
name|Searcher
name|engineSearcher
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|ElasticsearchDirectoryReader
name|elasticsearchDirectoryReader
init|=
name|ElasticsearchDirectoryReader
operator|.
name|getElasticsearchDirectoryReader
argument_list|(
name|engineSearcher
operator|.
name|getDirectoryReader
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|elasticsearchDirectoryReader
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Can't wrap non elasticsearch directory reader"
argument_list|)
throw|;
block|}
name|NonClosingReaderWrapper
name|nonClosingReaderWrapper
init|=
operator|new
name|NonClosingReaderWrapper
argument_list|(
name|engineSearcher
operator|.
name|getDirectoryReader
argument_list|()
argument_list|)
decl_stmt|;
name|DirectoryReader
name|reader
init|=
name|wrap
argument_list|(
name|nonClosingReaderWrapper
argument_list|)
decl_stmt|;
if|if
condition|(
name|reader
operator|!=
name|nonClosingReaderWrapper
condition|)
block|{
if|if
condition|(
name|reader
operator|.
name|getCoreCacheKey
argument_list|()
operator|!=
name|elasticsearchDirectoryReader
operator|.
name|getCoreCacheKey
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"wrapped directory reader doesn't delegate IndexReader#getCoreCacheKey, wrappers must override this method and delegate"
operator|+
literal|" to the original readers core cache key. Wrapped readers can't be used as cache keys since their are used only per request which would lead to subtle bugs"
argument_list|)
throw|;
block|}
if|if
condition|(
name|ElasticsearchDirectoryReader
operator|.
name|getElasticsearchDirectoryReader
argument_list|(
name|reader
argument_list|)
operator|!=
name|elasticsearchDirectoryReader
condition|)
block|{
comment|// prevent that somebody wraps with a non-filter reader
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"wrapped directory reader hides actual ElasticsearchDirectoryReader but shouldn't"
argument_list|)
throw|;
block|}
block|}
name|IndexSearcher
name|innerIndexSearcher
init|=
operator|new
name|IndexSearcher
argument_list|(
name|reader
argument_list|)
decl_stmt|;
name|innerIndexSearcher
operator|.
name|setQueryCache
argument_list|(
name|engineConfig
operator|.
name|getQueryCache
argument_list|()
argument_list|)
expr_stmt|;
name|innerIndexSearcher
operator|.
name|setQueryCachingPolicy
argument_list|(
name|engineConfig
operator|.
name|getQueryCachingPolicy
argument_list|()
argument_list|)
expr_stmt|;
name|innerIndexSearcher
operator|.
name|setSimilarity
argument_list|(
name|engineConfig
operator|.
name|getSimilarity
argument_list|()
argument_list|)
expr_stmt|;
comment|// TODO: Right now IndexSearcher isn't wrapper friendly, when it becomes wrapper friendly we should revise this extension point
comment|// For example if IndexSearcher#rewrite() is overwritten than also IndexSearcher#createNormalizedWeight needs to be overwritten
comment|// This needs to be fixed before we can allow the IndexSearcher from Engine to be wrapped multiple times
name|IndexSearcher
name|indexSearcher
init|=
name|wrap
argument_list|(
name|engineConfig
argument_list|,
name|innerIndexSearcher
argument_list|)
decl_stmt|;
if|if
condition|(
name|reader
operator|==
name|nonClosingReaderWrapper
operator|&&
name|indexSearcher
operator|==
name|innerIndexSearcher
condition|)
block|{
return|return
name|engineSearcher
return|;
block|}
else|else
block|{
specifier|final
name|Engine
operator|.
name|Searcher
name|newSearcher
init|=
operator|new
name|Engine
operator|.
name|Searcher
argument_list|(
name|engineSearcher
operator|.
name|source
argument_list|()
argument_list|,
name|indexSearcher
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|ElasticsearchException
block|{
try|try
block|{
name|reader
argument_list|()
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// we close the reader to make sure wrappers can release resources if needed....
comment|// our NonClosingReaderWrapper makes sure that our reader is not closed
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
literal|"failed to close reader"
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|engineSearcher
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
decl_stmt|;
return|return
name|newSearcher
return|;
block|}
block|}
DECL|class|NonClosingReaderWrapper
specifier|private
specifier|static
specifier|final
class|class
name|NonClosingReaderWrapper
extends|extends
name|FilterDirectoryReader
block|{
DECL|method|NonClosingReaderWrapper
specifier|private
name|NonClosingReaderWrapper
parameter_list|(
name|DirectoryReader
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|in
argument_list|,
operator|new
name|SubReaderWrapper
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|LeafReader
name|wrap
parameter_list|(
name|LeafReader
name|reader
parameter_list|)
block|{
return|return
name|reader
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doWrapDirectoryReader
specifier|protected
name|DirectoryReader
name|doWrapDirectoryReader
parameter_list|(
name|DirectoryReader
name|in
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|NonClosingReaderWrapper
argument_list|(
name|in
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doClose
specifier|protected
name|void
name|doClose
parameter_list|()
throws|throws
name|IOException
block|{
comment|// don't close here - mimic the MultiReader#doClose = false behavior that FilterDirectoryReader doesn't have
block|}
annotation|@
name|Override
DECL|method|getCoreCacheKey
specifier|public
name|Object
name|getCoreCacheKey
parameter_list|()
block|{
return|return
name|in
operator|.
name|getCoreCacheKey
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

