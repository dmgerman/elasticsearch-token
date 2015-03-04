begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.engine
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|engine
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
name|logging
operator|.
name|ESLogger
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
name|shard
operator|.
name|ShardId
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

begin_comment
comment|/**  * A searcher that asserts the IndexReader's refcount on close  */
end_comment

begin_class
DECL|class|AssertingSearcher
specifier|public
class|class
name|AssertingSearcher
extends|extends
name|Engine
operator|.
name|Searcher
block|{
DECL|field|wrappedSearcher
specifier|private
specifier|final
name|Engine
operator|.
name|Searcher
name|wrappedSearcher
decl_stmt|;
DECL|field|shardId
specifier|private
specifier|final
name|ShardId
name|shardId
decl_stmt|;
DECL|field|indexSearcher
specifier|private
specifier|final
name|IndexSearcher
name|indexSearcher
decl_stmt|;
DECL|field|firstReleaseStack
specifier|private
name|RuntimeException
name|firstReleaseStack
decl_stmt|;
DECL|field|lock
specifier|private
specifier|final
name|Object
name|lock
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
DECL|field|initialRefCount
specifier|private
specifier|final
name|int
name|initialRefCount
decl_stmt|;
DECL|field|logger
specifier|private
specifier|final
name|ESLogger
name|logger
decl_stmt|;
DECL|field|inFlightSearchers
specifier|private
specifier|final
name|Map
argument_list|<
name|AssertingSearcher
argument_list|,
name|RuntimeException
argument_list|>
name|inFlightSearchers
decl_stmt|;
DECL|method|AssertingSearcher
specifier|public
name|AssertingSearcher
parameter_list|(
name|IndexSearcher
name|indexSearcher
parameter_list|,
name|Engine
operator|.
name|Searcher
name|wrappedSearcher
parameter_list|,
name|ShardId
name|shardId
parameter_list|,
name|Map
argument_list|<
name|AssertingSearcher
argument_list|,
name|RuntimeException
argument_list|>
name|inFlightSearchers
parameter_list|,
name|ESLogger
name|logger
parameter_list|)
block|{
name|super
argument_list|(
name|wrappedSearcher
operator|.
name|source
argument_list|()
argument_list|,
name|indexSearcher
argument_list|)
expr_stmt|;
comment|// we only use the given index searcher here instead of the IS of the wrapped searcher. the IS might be a wrapped searcher
comment|// with a wrapped reader.
name|this
operator|.
name|wrappedSearcher
operator|=
name|wrappedSearcher
expr_stmt|;
name|this
operator|.
name|logger
operator|=
name|logger
expr_stmt|;
name|this
operator|.
name|shardId
operator|=
name|shardId
expr_stmt|;
name|initialRefCount
operator|=
name|wrappedSearcher
operator|.
name|reader
argument_list|()
operator|.
name|getRefCount
argument_list|()
expr_stmt|;
name|this
operator|.
name|indexSearcher
operator|=
name|indexSearcher
expr_stmt|;
assert|assert
name|initialRefCount
operator|>
literal|0
operator|:
literal|"IndexReader#getRefCount() was ["
operator|+
name|initialRefCount
operator|+
literal|"] expected a value> [0] - reader is already closed"
assert|;
name|this
operator|.
name|inFlightSearchers
operator|=
name|inFlightSearchers
expr_stmt|;
name|this
operator|.
name|inFlightSearchers
operator|.
name|put
argument_list|(
name|this
argument_list|,
operator|new
name|RuntimeException
argument_list|(
literal|"Unreleased Searcher, source ["
operator|+
name|wrappedSearcher
operator|.
name|source
argument_list|()
operator|+
literal|"]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|source
specifier|public
name|String
name|source
parameter_list|()
block|{
return|return
name|wrappedSearcher
operator|.
name|source
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|ElasticsearchException
block|{
name|RuntimeException
name|remove
init|=
name|inFlightSearchers
operator|.
name|remove
argument_list|(
name|this
argument_list|)
decl_stmt|;
synchronized|synchronized
init|(
name|lock
init|)
block|{
comment|// make sure we only get this once and store the stack of the first caller!
if|if
condition|(
name|remove
operator|==
literal|null
condition|)
block|{
assert|assert
name|firstReleaseStack
operator|!=
literal|null
assert|;
name|AssertionError
name|error
init|=
operator|new
name|AssertionError
argument_list|(
literal|"Released Searcher more than once, source ["
operator|+
name|wrappedSearcher
operator|.
name|source
argument_list|()
operator|+
literal|"]"
argument_list|)
decl_stmt|;
name|error
operator|.
name|initCause
argument_list|(
name|firstReleaseStack
argument_list|)
expr_stmt|;
throw|throw
name|error
throw|;
block|}
else|else
block|{
assert|assert
name|firstReleaseStack
operator|==
literal|null
assert|;
name|firstReleaseStack
operator|=
operator|new
name|RuntimeException
argument_list|(
literal|"Searcher Released first here, source ["
operator|+
name|wrappedSearcher
operator|.
name|source
argument_list|()
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
block|}
specifier|final
name|int
name|refCount
init|=
name|wrappedSearcher
operator|.
name|reader
argument_list|()
operator|.
name|getRefCount
argument_list|()
decl_stmt|;
comment|// this assert seems to be paranoid but given LUCENE-5362 we better add some assertions here to make sure we catch any potential
comment|// problems.
assert|assert
name|refCount
operator|>
literal|0
operator|:
literal|"IndexReader#getRefCount() was ["
operator|+
name|refCount
operator|+
literal|"] expected a value> [0] - reader is already closed. Initial refCount was: ["
operator|+
name|initialRefCount
operator|+
literal|"]"
assert|;
try|try
block|{
name|wrappedSearcher
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RuntimeException
name|ex
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"Failed to release searcher"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
throw|throw
name|ex
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|reader
specifier|public
name|IndexReader
name|reader
parameter_list|()
block|{
return|return
name|indexSearcher
operator|.
name|getIndexReader
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|searcher
specifier|public
name|IndexSearcher
name|searcher
parameter_list|()
block|{
return|return
name|indexSearcher
return|;
block|}
DECL|method|shardId
specifier|public
name|ShardId
name|shardId
parameter_list|()
block|{
return|return
name|shardId
return|;
block|}
block|}
end_class

end_unit

