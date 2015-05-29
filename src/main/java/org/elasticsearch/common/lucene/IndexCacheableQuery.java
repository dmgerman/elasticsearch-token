begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.lucene
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|lucene
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
name|Weight
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
name|Objects
import|;
end_import

begin_comment
comment|/**  * Base implementation for a query which is cacheable at the index level but  * not the segment level as usually expected.  */
end_comment

begin_class
DECL|class|IndexCacheableQuery
specifier|public
specifier|abstract
class|class
name|IndexCacheableQuery
extends|extends
name|Query
block|{
DECL|field|readerCacheKey
specifier|private
name|Object
name|readerCacheKey
decl_stmt|;
annotation|@
name|Override
DECL|method|rewrite
specifier|public
name|Query
name|rewrite
parameter_list|(
name|IndexReader
name|reader
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|reader
operator|.
name|getCoreCacheKey
argument_list|()
operator|!=
name|this
operator|.
name|readerCacheKey
condition|)
block|{
name|IndexCacheableQuery
name|rewritten
init|=
operator|(
name|IndexCacheableQuery
operator|)
name|clone
argument_list|()
decl_stmt|;
name|rewritten
operator|.
name|readerCacheKey
operator|=
name|reader
operator|.
name|getCoreCacheKey
argument_list|()
expr_stmt|;
return|return
name|rewritten
return|;
block|}
return|return
name|super
operator|.
name|rewrite
argument_list|(
name|reader
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
return|return
name|super
operator|.
name|equals
argument_list|(
name|obj
argument_list|)
operator|&&
name|readerCacheKey
operator|==
operator|(
operator|(
name|IndexCacheableQuery
operator|)
name|obj
operator|)
operator|.
name|readerCacheKey
return|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
literal|31
operator|*
name|super
operator|.
name|hashCode
argument_list|()
operator|+
name|Objects
operator|.
name|hashCode
argument_list|(
name|readerCacheKey
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|createWeight
specifier|public
specifier|final
name|Weight
name|createWeight
parameter_list|(
name|IndexSearcher
name|searcher
parameter_list|,
name|boolean
name|needsScores
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|readerCacheKey
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Rewrite first"
argument_list|)
throw|;
block|}
if|if
condition|(
name|readerCacheKey
operator|!=
name|searcher
operator|.
name|getIndexReader
argument_list|()
operator|.
name|getCoreCacheKey
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Must create weight on the same reader which has been used for rewriting"
argument_list|)
throw|;
block|}
return|return
name|doCreateWeight
argument_list|(
name|searcher
argument_list|,
name|needsScores
argument_list|)
return|;
block|}
comment|/** Create a {@link Weight} for this query.      *  @see Query#createWeight(IndexSearcher, boolean)      */
DECL|method|doCreateWeight
specifier|public
specifier|abstract
name|Weight
name|doCreateWeight
parameter_list|(
name|IndexSearcher
name|searcher
parameter_list|,
name|boolean
name|needsScores
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_class

end_unit
