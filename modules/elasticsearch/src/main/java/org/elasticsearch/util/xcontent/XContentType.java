begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.util.xcontent
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|xcontent
package|;
end_package

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_enum
DECL|enum|XContentType
specifier|public
enum|enum
name|XContentType
block|{
DECL|enum constant|JSON
name|JSON
argument_list|(
literal|0
argument_list|)
block|;
DECL|field|index
specifier|private
name|int
name|index
decl_stmt|;
DECL|method|XContentType
name|XContentType
parameter_list|(
name|int
name|index
parameter_list|)
block|{
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
block|}
DECL|method|index
specifier|public
name|int
name|index
parameter_list|()
block|{
return|return
name|index
return|;
block|}
block|}
end_enum

end_unit

