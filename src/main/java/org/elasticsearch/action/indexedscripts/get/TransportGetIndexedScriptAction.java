begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.indexedscripts.get
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|indexedscripts
operator|.
name|get
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
name|get
operator|.
name|GetResponse
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
name|script
operator|.
name|ScriptService
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

begin_comment
comment|/**  * Performs the get operation.  */
end_comment

begin_class
DECL|class|TransportGetIndexedScriptAction
specifier|public
class|class
name|TransportGetIndexedScriptAction
extends|extends
name|HandledTransportAction
argument_list|<
name|GetIndexedScriptRequest
argument_list|,
name|GetIndexedScriptResponse
argument_list|>
block|{
DECL|field|scriptService
specifier|private
specifier|final
name|ScriptService
name|scriptService
decl_stmt|;
DECL|field|client
specifier|private
specifier|final
name|Client
name|client
decl_stmt|;
annotation|@
name|Inject
DECL|method|TransportGetIndexedScriptAction
specifier|public
name|TransportGetIndexedScriptAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|ScriptService
name|scriptService
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|Client
name|client
parameter_list|,
name|ActionFilters
name|actionFilters
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|GetIndexedScriptAction
operator|.
name|NAME
argument_list|,
name|threadPool
argument_list|,
name|transportService
argument_list|,
name|actionFilters
argument_list|)
expr_stmt|;
name|this
operator|.
name|scriptService
operator|=
name|scriptService
expr_stmt|;
name|this
operator|.
name|client
operator|=
name|client
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|newRequestInstance
specifier|public
name|GetIndexedScriptRequest
name|newRequestInstance
parameter_list|()
block|{
return|return
operator|new
name|GetIndexedScriptRequest
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|doExecute
specifier|public
name|void
name|doExecute
parameter_list|(
name|GetIndexedScriptRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|GetIndexedScriptResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|GetResponse
name|scriptResponse
init|=
name|scriptService
operator|.
name|queryScriptIndex
argument_list|(
name|client
argument_list|,
name|request
operator|.
name|scriptLang
argument_list|()
argument_list|,
name|request
operator|.
name|id
argument_list|()
argument_list|,
name|request
operator|.
name|version
argument_list|()
argument_list|,
name|request
operator|.
name|versionType
argument_list|()
argument_list|)
decl_stmt|;
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|GetIndexedScriptResponse
argument_list|(
name|scriptResponse
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

