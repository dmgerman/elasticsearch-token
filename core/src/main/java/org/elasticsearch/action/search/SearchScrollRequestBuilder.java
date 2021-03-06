begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.search
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|search
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
name|ActionRequestBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|ElasticsearchClient
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
name|unit
operator|.
name|TimeValue
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
name|Scroll
import|;
end_import

begin_comment
comment|/**  * A search scroll action request builder.  */
end_comment

begin_class
DECL|class|SearchScrollRequestBuilder
specifier|public
class|class
name|SearchScrollRequestBuilder
extends|extends
name|ActionRequestBuilder
argument_list|<
name|SearchScrollRequest
argument_list|,
name|SearchResponse
argument_list|,
name|SearchScrollRequestBuilder
argument_list|>
block|{
DECL|method|SearchScrollRequestBuilder
specifier|public
name|SearchScrollRequestBuilder
parameter_list|(
name|ElasticsearchClient
name|client
parameter_list|,
name|SearchScrollAction
name|action
parameter_list|)
block|{
name|super
argument_list|(
name|client
argument_list|,
name|action
argument_list|,
operator|new
name|SearchScrollRequest
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|SearchScrollRequestBuilder
specifier|public
name|SearchScrollRequestBuilder
parameter_list|(
name|ElasticsearchClient
name|client
parameter_list|,
name|SearchScrollAction
name|action
parameter_list|,
name|String
name|scrollId
parameter_list|)
block|{
name|super
argument_list|(
name|client
argument_list|,
name|action
argument_list|,
operator|new
name|SearchScrollRequest
argument_list|(
name|scrollId
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * The scroll id to use to continue scrolling.      */
DECL|method|setScrollId
specifier|public
name|SearchScrollRequestBuilder
name|setScrollId
parameter_list|(
name|String
name|scrollId
parameter_list|)
block|{
name|request
operator|.
name|scrollId
argument_list|(
name|scrollId
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * If set, will enable scrolling of the search request.      */
DECL|method|setScroll
specifier|public
name|SearchScrollRequestBuilder
name|setScroll
parameter_list|(
name|Scroll
name|scroll
parameter_list|)
block|{
name|request
operator|.
name|scroll
argument_list|(
name|scroll
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * If set, will enable scrolling of the search request for the specified timeout.      */
DECL|method|setScroll
specifier|public
name|SearchScrollRequestBuilder
name|setScroll
parameter_list|(
name|TimeValue
name|keepAlive
parameter_list|)
block|{
name|request
operator|.
name|scroll
argument_list|(
name|keepAlive
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * If set, will enable scrolling of the search request for the specified timeout.      */
DECL|method|setScroll
specifier|public
name|SearchScrollRequestBuilder
name|setScroll
parameter_list|(
name|String
name|keepAlive
parameter_list|)
block|{
name|request
operator|.
name|scroll
argument_list|(
name|keepAlive
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
block|}
end_class

end_unit

