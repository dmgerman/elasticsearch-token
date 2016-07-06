begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.search.template
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|search
operator|.
name|template
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|ActionListener
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
name|support
operator|.
name|ActionFilters
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
name|support
operator|.
name|HandledTransportAction
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
operator|.
name|IndexNameExpressionResolver
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
name|inject
operator|.
name|Inject
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
name|settings
operator|.
name|Settings
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
name|util
operator|.
name|concurrent
operator|.
name|AtomicArray
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|threadpool
operator|.
name|ThreadPool
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|TransportService
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicInteger
import|;
end_import

begin_class
DECL|class|TransportMultiSearchTemplateAction
specifier|public
class|class
name|TransportMultiSearchTemplateAction
extends|extends
name|HandledTransportAction
argument_list|<
name|MultiSearchTemplateRequest
argument_list|,
name|MultiSearchTemplateResponse
argument_list|>
block|{
DECL|field|searchTemplateAction
specifier|private
specifier|final
name|TransportSearchTemplateAction
name|searchTemplateAction
decl_stmt|;
annotation|@
name|Inject
DECL|method|TransportMultiSearchTemplateAction
specifier|public
name|TransportMultiSearchTemplateAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|ActionFilters
name|actionFilters
parameter_list|,
name|IndexNameExpressionResolver
name|resolver
parameter_list|,
name|TransportSearchTemplateAction
name|searchTemplateAction
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|MultiSearchTemplateAction
operator|.
name|NAME
argument_list|,
name|threadPool
argument_list|,
name|transportService
argument_list|,
name|actionFilters
argument_list|,
name|resolver
argument_list|,
name|MultiSearchTemplateRequest
operator|::
operator|new
argument_list|)
expr_stmt|;
name|this
operator|.
name|searchTemplateAction
operator|=
name|searchTemplateAction
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doExecute
specifier|protected
name|void
name|doExecute
parameter_list|(
name|MultiSearchTemplateRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|MultiSearchTemplateResponse
argument_list|>
name|listener
parameter_list|)
block|{
specifier|final
name|AtomicArray
argument_list|<
name|MultiSearchTemplateResponse
operator|.
name|Item
argument_list|>
name|responses
init|=
operator|new
name|AtomicArray
argument_list|<>
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|AtomicInteger
name|counter
init|=
operator|new
name|AtomicInteger
argument_list|(
name|responses
operator|.
name|length
argument_list|()
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
name|responses
operator|.
name|length
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|int
name|index
init|=
name|i
decl_stmt|;
name|searchTemplateAction
operator|.
name|execute
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|SearchTemplateResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|SearchTemplateResponse
name|searchTemplateResponse
parameter_list|)
block|{
name|responses
operator|.
name|set
argument_list|(
name|index
argument_list|,
operator|new
name|MultiSearchTemplateResponse
operator|.
name|Item
argument_list|(
name|searchTemplateResponse
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|counter
operator|.
name|decrementAndGet
argument_list|()
operator|==
literal|0
condition|)
block|{
name|finishHim
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|responses
operator|.
name|set
argument_list|(
name|index
argument_list|,
operator|new
name|MultiSearchTemplateResponse
operator|.
name|Item
argument_list|(
literal|null
argument_list|,
name|e
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|counter
operator|.
name|decrementAndGet
argument_list|()
operator|==
literal|0
condition|)
block|{
name|finishHim
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|finishHim
parameter_list|()
block|{
name|MultiSearchTemplateResponse
operator|.
name|Item
index|[]
name|items
init|=
name|responses
operator|.
name|toArray
argument_list|(
operator|new
name|MultiSearchTemplateResponse
operator|.
name|Item
index|[
name|responses
operator|.
name|length
argument_list|()
index|]
argument_list|)
decl_stmt|;
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|MultiSearchTemplateResponse
argument_list|(
name|items
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

