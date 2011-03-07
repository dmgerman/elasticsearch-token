begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.script
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|script
package|;
end_package

begin_class
DECL|class|AbstractFloatSearchScript
specifier|public
specifier|abstract
class|class
name|AbstractFloatSearchScript
extends|extends
name|AbstractSearchScript
block|{
DECL|method|run
annotation|@
name|Override
specifier|public
name|Object
name|run
parameter_list|()
block|{
return|return
name|runAsFloat
argument_list|()
return|;
block|}
DECL|method|runAsFloat
annotation|@
name|Override
specifier|public
specifier|abstract
name|float
name|runAsFloat
parameter_list|()
function_decl|;
DECL|method|runAsDouble
annotation|@
name|Override
specifier|public
name|double
name|runAsDouble
parameter_list|()
block|{
return|return
name|runAsFloat
argument_list|()
return|;
block|}
DECL|method|runAsLong
annotation|@
name|Override
specifier|public
name|long
name|runAsLong
parameter_list|()
block|{
return|return
operator|(
name|long
operator|)
name|runAsFloat
argument_list|()
return|;
block|}
block|}
end_class

end_unit

