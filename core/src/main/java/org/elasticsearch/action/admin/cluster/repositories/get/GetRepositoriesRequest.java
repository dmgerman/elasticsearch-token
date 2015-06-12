begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.cluster.repositories.get
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|cluster
operator|.
name|repositories
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
name|ActionRequestValidationException
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
name|master
operator|.
name|MasterNodeReadRequest
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|io
operator|.
name|stream
operator|.
name|StreamOutput
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|ValidateActions
operator|.
name|addValidationError
import|;
end_import

begin_comment
comment|/**  * Get repository request  */
end_comment

begin_class
DECL|class|GetRepositoriesRequest
specifier|public
class|class
name|GetRepositoriesRequest
extends|extends
name|MasterNodeReadRequest
argument_list|<
name|GetRepositoriesRequest
argument_list|>
block|{
DECL|field|repositories
specifier|private
name|String
index|[]
name|repositories
init|=
name|Strings
operator|.
name|EMPTY_ARRAY
decl_stmt|;
DECL|method|GetRepositoriesRequest
name|GetRepositoriesRequest
parameter_list|()
block|{     }
comment|/**      * Constructs a new get repositories request with a list of repositories.      *<p/>      * If the list of repositories is empty or it contains a single element "_all", all registered repositories      * are returned.      *      * @param repositories list of repositories      */
DECL|method|GetRepositoriesRequest
specifier|public
name|GetRepositoriesRequest
parameter_list|(
name|String
index|[]
name|repositories
parameter_list|)
block|{
name|this
operator|.
name|repositories
operator|=
name|repositories
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|validate
specifier|public
name|ActionRequestValidationException
name|validate
parameter_list|()
block|{
name|ActionRequestValidationException
name|validationException
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|repositories
operator|==
literal|null
condition|)
block|{
name|validationException
operator|=
name|addValidationError
argument_list|(
literal|"repositories is null"
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
return|return
name|validationException
return|;
block|}
comment|/**      * The names of the repositories.      *      * @return list of repositories      */
DECL|method|repositories
specifier|public
name|String
index|[]
name|repositories
parameter_list|()
block|{
return|return
name|this
operator|.
name|repositories
return|;
block|}
comment|/**      * Sets the list or repositories.      *<p/>      * If the list of repositories is empty or it contains a single element "_all", all registered repositories      * are returned.      *      * @param repositories list of repositories      * @return this request      */
DECL|method|repositories
specifier|public
name|GetRepositoriesRequest
name|repositories
parameter_list|(
name|String
index|[]
name|repositories
parameter_list|)
block|{
name|this
operator|.
name|repositories
operator|=
name|repositories
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|repositories
operator|=
name|in
operator|.
name|readStringArray
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeStringArray
argument_list|(
name|repositories
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
