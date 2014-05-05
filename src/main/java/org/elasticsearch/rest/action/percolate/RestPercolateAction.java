begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.rest.action.percolate
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|action
operator|.
name|percolate
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
name|percolate
operator|.
name|PercolateRequest
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
name|percolate
operator|.
name|PercolateResponse
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
name|IndicesOptions
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
name|Client
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
name|Strings
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
name|index
operator|.
name|VersionType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
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
name|rest
operator|.
name|action
operator|.
name|support
operator|.
name|RestActions
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|action
operator|.
name|support
operator|.
name|RestToXContentListener
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestRequest
operator|.
name|Method
operator|.
name|GET
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestRequest
operator|.
name|Method
operator|.
name|POST
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|RestPercolateAction
specifier|public
class|class
name|RestPercolateAction
extends|extends
name|BaseRestHandler
block|{
annotation|@
name|Inject
DECL|method|RestPercolateAction
specifier|public
name|RestPercolateAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|Client
name|client
parameter_list|,
name|RestController
name|controller
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|client
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|GET
argument_list|,
literal|"/{index}/{type}/_percolate"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|POST
argument_list|,
literal|"/{index}/{type}/_percolate"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|RestPercolateExistingDocHandler
name|existingDocHandler
init|=
operator|new
name|RestPercolateExistingDocHandler
argument_list|()
decl_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|GET
argument_list|,
literal|"/{index}/{type}/{id}/_percolate"
argument_list|,
name|existingDocHandler
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|POST
argument_list|,
literal|"/{index}/{type}/{id}/_percolate"
argument_list|,
name|existingDocHandler
argument_list|)
expr_stmt|;
name|RestCountPercolateDocHandler
name|countHandler
init|=
operator|new
name|RestCountPercolateDocHandler
argument_list|()
decl_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|GET
argument_list|,
literal|"/{index}/{type}/_percolate/count"
argument_list|,
name|countHandler
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|POST
argument_list|,
literal|"/{index}/{type}/_percolate/count"
argument_list|,
name|countHandler
argument_list|)
expr_stmt|;
name|RestCountPercolateExistingDocHandler
name|countExistingDocHandler
init|=
operator|new
name|RestCountPercolateExistingDocHandler
argument_list|()
decl_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|GET
argument_list|,
literal|"/{index}/{type}/{id}/_percolate/count"
argument_list|,
name|countExistingDocHandler
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|POST
argument_list|,
literal|"/{index}/{type}/{id}/_percolate/count"
argument_list|,
name|countExistingDocHandler
argument_list|)
expr_stmt|;
block|}
DECL|method|parseDocPercolate
name|void
name|parseDocPercolate
parameter_list|(
name|PercolateRequest
name|percolateRequest
parameter_list|,
name|RestRequest
name|restRequest
parameter_list|,
name|RestChannel
name|restChannel
parameter_list|)
block|{
name|percolateRequest
operator|.
name|indices
argument_list|(
name|Strings
operator|.
name|splitStringByCommaToArray
argument_list|(
name|restRequest
operator|.
name|param
argument_list|(
literal|"index"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|percolateRequest
operator|.
name|documentType
argument_list|(
name|restRequest
operator|.
name|param
argument_list|(
literal|"type"
argument_list|)
argument_list|)
expr_stmt|;
name|percolateRequest
operator|.
name|routing
argument_list|(
name|restRequest
operator|.
name|param
argument_list|(
literal|"routing"
argument_list|)
argument_list|)
expr_stmt|;
name|percolateRequest
operator|.
name|preference
argument_list|(
name|restRequest
operator|.
name|param
argument_list|(
literal|"preference"
argument_list|)
argument_list|)
expr_stmt|;
name|percolateRequest
operator|.
name|source
argument_list|(
name|RestActions
operator|.
name|getRestContent
argument_list|(
name|restRequest
argument_list|)
argument_list|,
name|restRequest
operator|.
name|contentUnsafe
argument_list|()
argument_list|)
expr_stmt|;
name|percolateRequest
operator|.
name|indicesOptions
argument_list|(
name|IndicesOptions
operator|.
name|fromRequest
argument_list|(
name|restRequest
argument_list|,
name|percolateRequest
operator|.
name|indicesOptions
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|executePercolate
argument_list|(
name|percolateRequest
argument_list|,
name|restRequest
argument_list|,
name|restChannel
argument_list|)
expr_stmt|;
block|}
DECL|method|parseExistingDocPercolate
name|void
name|parseExistingDocPercolate
parameter_list|(
name|PercolateRequest
name|percolateRequest
parameter_list|,
name|RestRequest
name|restRequest
parameter_list|,
name|RestChannel
name|restChannel
parameter_list|)
block|{
name|String
name|index
init|=
name|restRequest
operator|.
name|param
argument_list|(
literal|"index"
argument_list|)
decl_stmt|;
name|String
name|type
init|=
name|restRequest
operator|.
name|param
argument_list|(
literal|"type"
argument_list|)
decl_stmt|;
name|percolateRequest
operator|.
name|indices
argument_list|(
name|Strings
operator|.
name|splitStringByCommaToArray
argument_list|(
name|restRequest
operator|.
name|param
argument_list|(
literal|"percolate_index"
argument_list|,
name|index
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|percolateRequest
operator|.
name|documentType
argument_list|(
name|restRequest
operator|.
name|param
argument_list|(
literal|"percolate_type"
argument_list|,
name|type
argument_list|)
argument_list|)
expr_stmt|;
name|GetRequest
name|getRequest
init|=
operator|new
name|GetRequest
argument_list|(
name|index
argument_list|,
name|type
argument_list|,
name|restRequest
operator|.
name|param
argument_list|(
literal|"id"
argument_list|)
argument_list|)
decl_stmt|;
name|getRequest
operator|.
name|routing
argument_list|(
name|restRequest
operator|.
name|param
argument_list|(
literal|"routing"
argument_list|)
argument_list|)
expr_stmt|;
name|getRequest
operator|.
name|preference
argument_list|(
name|restRequest
operator|.
name|param
argument_list|(
literal|"preference"
argument_list|)
argument_list|)
expr_stmt|;
name|getRequest
operator|.
name|refresh
argument_list|(
name|restRequest
operator|.
name|paramAsBoolean
argument_list|(
literal|"refresh"
argument_list|,
name|getRequest
operator|.
name|refresh
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|getRequest
operator|.
name|realtime
argument_list|(
name|restRequest
operator|.
name|paramAsBoolean
argument_list|(
literal|"realtime"
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|getRequest
operator|.
name|version
argument_list|(
name|RestActions
operator|.
name|parseVersion
argument_list|(
name|restRequest
argument_list|)
argument_list|)
expr_stmt|;
name|getRequest
operator|.
name|versionType
argument_list|(
name|VersionType
operator|.
name|fromString
argument_list|(
name|restRequest
operator|.
name|param
argument_list|(
literal|"version_type"
argument_list|)
argument_list|,
name|getRequest
operator|.
name|versionType
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|percolateRequest
operator|.
name|getRequest
argument_list|(
name|getRequest
argument_list|)
expr_stmt|;
name|percolateRequest
operator|.
name|routing
argument_list|(
name|restRequest
operator|.
name|param
argument_list|(
literal|"percolate_routing"
argument_list|)
argument_list|)
expr_stmt|;
name|percolateRequest
operator|.
name|preference
argument_list|(
name|restRequest
operator|.
name|param
argument_list|(
literal|"percolate_preference"
argument_list|)
argument_list|)
expr_stmt|;
name|percolateRequest
operator|.
name|source
argument_list|(
name|restRequest
operator|.
name|content
argument_list|()
argument_list|,
name|restRequest
operator|.
name|contentUnsafe
argument_list|()
argument_list|)
expr_stmt|;
name|percolateRequest
operator|.
name|indicesOptions
argument_list|(
name|IndicesOptions
operator|.
name|fromRequest
argument_list|(
name|restRequest
argument_list|,
name|percolateRequest
operator|.
name|indicesOptions
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|executePercolate
argument_list|(
name|percolateRequest
argument_list|,
name|restRequest
argument_list|,
name|restChannel
argument_list|)
expr_stmt|;
block|}
DECL|method|executePercolate
name|void
name|executePercolate
parameter_list|(
specifier|final
name|PercolateRequest
name|percolateRequest
parameter_list|,
specifier|final
name|RestRequest
name|restRequest
parameter_list|,
specifier|final
name|RestChannel
name|restChannel
parameter_list|)
block|{
comment|// we just send a response, no need to fork
name|percolateRequest
operator|.
name|listenerThreaded
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|client
operator|.
name|percolate
argument_list|(
name|percolateRequest
argument_list|,
operator|new
name|RestToXContentListener
argument_list|<
name|PercolateResponse
argument_list|>
argument_list|(
name|restChannel
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|handleRequest
specifier|public
name|void
name|handleRequest
parameter_list|(
name|RestRequest
name|restRequest
parameter_list|,
name|RestChannel
name|restChannel
parameter_list|)
block|{
name|PercolateRequest
name|percolateRequest
init|=
operator|new
name|PercolateRequest
argument_list|()
decl_stmt|;
name|parseDocPercolate
argument_list|(
name|percolateRequest
argument_list|,
name|restRequest
argument_list|,
name|restChannel
argument_list|)
expr_stmt|;
block|}
DECL|class|RestCountPercolateDocHandler
specifier|final
class|class
name|RestCountPercolateDocHandler
implements|implements
name|RestHandler
block|{
annotation|@
name|Override
DECL|method|handleRequest
specifier|public
name|void
name|handleRequest
parameter_list|(
name|RestRequest
name|restRequest
parameter_list|,
name|RestChannel
name|restChannel
parameter_list|)
block|{
name|PercolateRequest
name|percolateRequest
init|=
operator|new
name|PercolateRequest
argument_list|()
decl_stmt|;
name|percolateRequest
operator|.
name|onlyCount
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|parseDocPercolate
argument_list|(
name|percolateRequest
argument_list|,
name|restRequest
argument_list|,
name|restChannel
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|RestPercolateExistingDocHandler
specifier|final
class|class
name|RestPercolateExistingDocHandler
implements|implements
name|RestHandler
block|{
annotation|@
name|Override
DECL|method|handleRequest
specifier|public
name|void
name|handleRequest
parameter_list|(
name|RestRequest
name|restRequest
parameter_list|,
name|RestChannel
name|restChannel
parameter_list|)
block|{
name|PercolateRequest
name|percolateRequest
init|=
operator|new
name|PercolateRequest
argument_list|()
decl_stmt|;
name|parseExistingDocPercolate
argument_list|(
name|percolateRequest
argument_list|,
name|restRequest
argument_list|,
name|restChannel
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|RestCountPercolateExistingDocHandler
specifier|final
class|class
name|RestCountPercolateExistingDocHandler
implements|implements
name|RestHandler
block|{
annotation|@
name|Override
DECL|method|handleRequest
specifier|public
name|void
name|handleRequest
parameter_list|(
name|RestRequest
name|restRequest
parameter_list|,
name|RestChannel
name|restChannel
parameter_list|)
block|{
name|PercolateRequest
name|percolateRequest
init|=
operator|new
name|PercolateRequest
argument_list|()
decl_stmt|;
name|percolateRequest
operator|.
name|onlyCount
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|parseExistingDocPercolate
argument_list|(
name|percolateRequest
argument_list|,
name|restRequest
argument_list|,
name|restChannel
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

