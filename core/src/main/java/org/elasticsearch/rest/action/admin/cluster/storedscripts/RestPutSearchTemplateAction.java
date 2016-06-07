begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.rest.action.admin.cluster.storedscripts
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|action
operator|.
name|admin
operator|.
name|cluster
operator|.
name|storedscripts
package|;
end_package

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
name|rest
operator|.
name|BaseRestHandler
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
name|RestChannel
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
name|RestController
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
name|RestRequest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|Template
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
name|PUT
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|RestPutSearchTemplateAction
specifier|public
class|class
name|RestPutSearchTemplateAction
extends|extends
name|RestPutStoredScriptAction
block|{
annotation|@
name|Inject
DECL|method|RestPutSearchTemplateAction
specifier|public
name|RestPutSearchTemplateAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|RestController
name|controller
parameter_list|,
name|Client
name|client
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|controller
argument_list|,
literal|false
argument_list|,
name|client
argument_list|)
expr_stmt|;
comment|//controller.registerHandler(GET, "/template", this);
name|controller
operator|.
name|registerHandler
argument_list|(
name|POST
argument_list|,
literal|"/_search/template/{id}"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|PUT
argument_list|,
literal|"/_search/template/{id}"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|PUT
argument_list|,
literal|"/_search/template/{id}/_create"
argument_list|,
operator|new
name|CreateHandler
argument_list|(
name|settings
argument_list|,
name|controller
argument_list|,
name|client
argument_list|)
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|POST
argument_list|,
literal|"/_search/template/{id}/_create"
argument_list|,
operator|new
name|CreateHandler
argument_list|(
name|settings
argument_list|,
name|controller
argument_list|,
name|client
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|class|CreateHandler
specifier|final
class|class
name|CreateHandler
extends|extends
name|BaseRestHandler
block|{
DECL|method|CreateHandler
specifier|protected
name|CreateHandler
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|RestController
name|controller
parameter_list|,
name|Client
name|client
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|client
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
name|request
parameter_list|,
name|RestChannel
name|channel
parameter_list|,
specifier|final
name|Client
name|client
parameter_list|)
block|{
name|request
operator|.
name|params
argument_list|()
operator|.
name|put
argument_list|(
literal|"op_type"
argument_list|,
literal|"create"
argument_list|)
expr_stmt|;
name|RestPutSearchTemplateAction
operator|.
name|this
operator|.
name|handleRequest
argument_list|(
name|request
argument_list|,
name|channel
argument_list|,
name|client
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|getScriptLang
specifier|protected
name|String
name|getScriptLang
parameter_list|(
name|RestRequest
name|request
parameter_list|)
block|{
return|return
name|Template
operator|.
name|DEFAULT_LANG
return|;
block|}
block|}
end_class

end_unit
