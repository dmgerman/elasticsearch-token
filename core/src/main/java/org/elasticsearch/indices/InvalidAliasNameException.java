begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indices
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|Index
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
name|IndexException
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
name|RestStatus
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|InvalidAliasNameException
specifier|public
class|class
name|InvalidAliasNameException
extends|extends
name|IndexException
block|{
DECL|method|InvalidAliasNameException
specifier|public
name|InvalidAliasNameException
parameter_list|(
name|Index
name|index
parameter_list|,
name|String
name|name
parameter_list|,
name|String
name|desc
parameter_list|)
block|{
name|super
argument_list|(
name|index
argument_list|,
literal|"Invalid alias name ["
operator|+
name|name
operator|+
literal|"], "
operator|+
name|desc
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|status
specifier|public
name|RestStatus
name|status
parameter_list|()
block|{
return|return
name|RestStatus
operator|.
name|BAD_REQUEST
return|;
block|}
block|}
end_class

end_unit
