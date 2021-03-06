begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.engine
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
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
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|SearcherFactory
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
comment|/**  * Basic Searcher factory that allows returning an {@link IndexSearcher}  * given an {@link IndexReader}  */
end_comment

begin_class
DECL|class|EngineSearcherFactory
specifier|public
class|class
name|EngineSearcherFactory
extends|extends
name|SearcherFactory
block|{
DECL|field|engineConfig
specifier|private
specifier|final
name|EngineConfig
name|engineConfig
decl_stmt|;
DECL|method|EngineSearcherFactory
specifier|public
name|EngineSearcherFactory
parameter_list|(
name|EngineConfig
name|engineConfig
parameter_list|)
block|{
name|this
operator|.
name|engineConfig
operator|=
name|engineConfig
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|newSearcher
specifier|public
name|IndexSearcher
name|newSearcher
parameter_list|(
name|IndexReader
name|reader
parameter_list|,
name|IndexReader
name|previousReader
parameter_list|)
throws|throws
name|IOException
block|{
name|IndexSearcher
name|searcher
init|=
name|super
operator|.
name|newSearcher
argument_list|(
name|reader
argument_list|,
name|previousReader
argument_list|)
decl_stmt|;
name|searcher
operator|.
name|setQueryCache
argument_list|(
name|engineConfig
operator|.
name|getQueryCache
argument_list|()
argument_list|)
expr_stmt|;
name|searcher
operator|.
name|setQueryCachingPolicy
argument_list|(
name|engineConfig
operator|.
name|getQueryCachingPolicy
argument_list|()
argument_list|)
expr_stmt|;
name|searcher
operator|.
name|setSimilarity
argument_list|(
name|engineConfig
operator|.
name|getSimilarity
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|searcher
return|;
block|}
block|}
end_class

end_unit

