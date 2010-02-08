begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.server
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|server
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|server
operator|.
name|internal
operator|.
name|InternalServer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|settings
operator|.
name|ImmutableSettings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|settings
operator|.
name|Settings
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|ServerBuilder
specifier|public
class|class
name|ServerBuilder
block|{
DECL|field|settings
specifier|private
name|Settings
name|settings
init|=
name|ImmutableSettings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
decl_stmt|;
DECL|field|loadConfigSettings
specifier|private
name|boolean
name|loadConfigSettings
init|=
literal|true
decl_stmt|;
DECL|method|serverBuilder
specifier|public
specifier|static
name|ServerBuilder
name|serverBuilder
parameter_list|()
block|{
return|return
operator|new
name|ServerBuilder
argument_list|()
return|;
block|}
DECL|method|settings
specifier|public
name|ServerBuilder
name|settings
parameter_list|(
name|Settings
operator|.
name|Builder
name|settings
parameter_list|)
block|{
return|return
name|settings
argument_list|(
name|settings
operator|.
name|build
argument_list|()
argument_list|)
return|;
block|}
DECL|method|settings
specifier|public
name|ServerBuilder
name|settings
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|this
operator|.
name|settings
operator|=
name|settings
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|loadConfigSettings
specifier|public
name|ServerBuilder
name|loadConfigSettings
parameter_list|(
name|boolean
name|loadConfigSettings
parameter_list|)
block|{
name|this
operator|.
name|loadConfigSettings
operator|=
name|loadConfigSettings
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Builds the server without starting it.      */
DECL|method|build
specifier|public
name|Server
name|build
parameter_list|()
block|{
return|return
operator|new
name|InternalServer
argument_list|(
name|settings
argument_list|,
name|loadConfigSettings
argument_list|)
return|;
block|}
comment|/**      * {@link #build()}s and starts the server.      */
DECL|method|server
specifier|public
name|Server
name|server
parameter_list|()
block|{
return|return
name|build
argument_list|()
operator|.
name|start
argument_list|()
return|;
block|}
block|}
end_class

end_unit

