begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.lucene.index
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|lucene
operator|.
name|index
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
name|*
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
name|SuppressForbidden
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
name|shard
operator|.
name|ShardId
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
comment|/**  * A {@link org.apache.lucene.index.FilterDirectoryReader} that exposes  * Elasticsearch internal per shard / index information like the shard ID.  */
end_comment

begin_class
DECL|class|ElasticsearchDirectoryReader
specifier|public
specifier|final
class|class
name|ElasticsearchDirectoryReader
extends|extends
name|FilterDirectoryReader
block|{
DECL|field|shardId
specifier|private
specifier|final
name|ShardId
name|shardId
decl_stmt|;
DECL|field|wrapper
specifier|private
specifier|final
name|FilterDirectoryReader
operator|.
name|SubReaderWrapper
name|wrapper
decl_stmt|;
DECL|method|ElasticsearchDirectoryReader
specifier|private
name|ElasticsearchDirectoryReader
parameter_list|(
name|DirectoryReader
name|in
parameter_list|,
name|FilterDirectoryReader
operator|.
name|SubReaderWrapper
name|wrapper
parameter_list|,
name|ShardId
name|shardId
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|in
argument_list|,
name|wrapper
argument_list|)
expr_stmt|;
name|this
operator|.
name|wrapper
operator|=
name|wrapper
expr_stmt|;
name|this
operator|.
name|shardId
operator|=
name|shardId
expr_stmt|;
block|}
comment|/**      * Returns the shard id this index belongs to.      */
DECL|method|shardId
specifier|public
name|ShardId
name|shardId
parameter_list|()
block|{
return|return
name|this
operator|.
name|shardId
return|;
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
name|ElasticsearchDirectoryReader
argument_list|(
name|in
argument_list|,
name|wrapper
argument_list|,
name|shardId
argument_list|)
return|;
block|}
comment|/**      * Wraps the given reader in a {@link org.elasticsearch.common.lucene.index.ElasticsearchDirectoryReader} as      * well as all it's sub-readers in {@link org.elasticsearch.common.lucene.index.ElasticsearchLeafReader} to      * expose the given shard Id.      *      * @param reader the reader to wrap      * @param shardId the shard ID to expose via the elasticsearch internal reader wrappers.      */
DECL|method|wrap
specifier|public
specifier|static
name|ElasticsearchDirectoryReader
name|wrap
parameter_list|(
name|DirectoryReader
name|reader
parameter_list|,
name|ShardId
name|shardId
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|ElasticsearchDirectoryReader
argument_list|(
name|reader
argument_list|,
operator|new
name|SubReaderWrapper
argument_list|(
name|shardId
argument_list|)
argument_list|,
name|shardId
argument_list|)
return|;
block|}
DECL|class|SubReaderWrapper
specifier|private
specifier|final
specifier|static
class|class
name|SubReaderWrapper
extends|extends
name|FilterDirectoryReader
operator|.
name|SubReaderWrapper
block|{
DECL|field|shardId
specifier|private
specifier|final
name|ShardId
name|shardId
decl_stmt|;
DECL|method|SubReaderWrapper
name|SubReaderWrapper
parameter_list|(
name|ShardId
name|shardId
parameter_list|)
block|{
name|this
operator|.
name|shardId
operator|=
name|shardId
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|wrap
specifier|public
name|LeafReader
name|wrap
parameter_list|(
name|LeafReader
name|reader
parameter_list|)
block|{
return|return
operator|new
name|ElasticsearchLeafReader
argument_list|(
name|reader
argument_list|,
name|shardId
argument_list|)
return|;
block|}
block|}
comment|/**      * Adds the given listener to the provided directory reader. The reader must contain an {@link ElasticsearchDirectoryReader} in it's hierarchy      * otherwise we can't safely install the listener.      *      * @throws IllegalArgumentException if the reader doesn't contain an {@link ElasticsearchDirectoryReader} in it's hierarchy      */
annotation|@
name|SuppressForbidden
argument_list|(
name|reason
operator|=
literal|"This is the only sane way to add a ReaderClosedListener"
argument_list|)
DECL|method|addReaderCloseListener
specifier|public
specifier|static
name|void
name|addReaderCloseListener
parameter_list|(
name|DirectoryReader
name|reader
parameter_list|,
name|IndexReader
operator|.
name|ReaderClosedListener
name|listener
parameter_list|)
block|{
name|ElasticsearchDirectoryReader
name|elasticsearchDirectoryReader
init|=
name|getElasticsearchDirectoryReader
argument_list|(
name|reader
argument_list|)
decl_stmt|;
if|if
condition|(
name|elasticsearchDirectoryReader
operator|!=
literal|null
condition|)
block|{
assert|assert
name|reader
operator|.
name|getCoreCacheKey
argument_list|()
operator|==
name|elasticsearchDirectoryReader
operator|.
name|getCoreCacheKey
argument_list|()
assert|;
name|elasticsearchDirectoryReader
operator|.
name|addReaderClosedListener
argument_list|(
name|listener
argument_list|)
expr_stmt|;
return|return;
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Can't install close listener reader is not an ElasticsearchDirectoryReader/ElasticsearchLeafReader"
argument_list|)
throw|;
block|}
comment|/**      * Tries to unwrap the given reader until the first {@link ElasticsearchDirectoryReader} instance is found or<code>null</code> if no instance is found;      */
DECL|method|getElasticsearchDirectoryReader
specifier|public
specifier|static
name|ElasticsearchDirectoryReader
name|getElasticsearchDirectoryReader
parameter_list|(
name|DirectoryReader
name|reader
parameter_list|)
block|{
if|if
condition|(
name|reader
operator|instanceof
name|FilterDirectoryReader
condition|)
block|{
if|if
condition|(
name|reader
operator|instanceof
name|ElasticsearchDirectoryReader
condition|)
block|{
return|return
operator|(
name|ElasticsearchDirectoryReader
operator|)
name|reader
return|;
block|}
else|else
block|{
comment|// We need to use FilterDirectoryReader#getDelegate and not FilterDirectoryReader#unwrap, because
comment|// If there are multiple levels of filtered leaf readers then with the unwrap() method it immediately
comment|// returns the most inner leaf reader and thus skipping of over any other filtered leaf reader that
comment|// may be instance of ElasticsearchLeafReader. This can cause us to miss the shardId.
return|return
name|getElasticsearchDirectoryReader
argument_list|(
operator|(
operator|(
name|FilterDirectoryReader
operator|)
name|reader
operator|)
operator|.
name|getDelegate
argument_list|()
argument_list|)
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

