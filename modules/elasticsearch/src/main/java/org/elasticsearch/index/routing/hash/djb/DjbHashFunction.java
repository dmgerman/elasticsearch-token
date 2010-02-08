begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.routing.hash.djb
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|routing
operator|.
name|hash
operator|.
name|djb
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
name|routing
operator|.
name|hash
operator|.
name|HashFunction
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|DjbHashFunction
specifier|public
class|class
name|DjbHashFunction
implements|implements
name|HashFunction
block|{
DECL|method|hash
annotation|@
name|Override
specifier|public
name|int
name|hash
parameter_list|(
name|String
name|type
parameter_list|,
name|String
name|id
parameter_list|)
block|{
name|long
name|hash
init|=
literal|5381
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
name|type
operator|.
name|length
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|hash
operator|=
operator|(
operator|(
name|hash
operator|<<
literal|5
operator|)
operator|+
name|hash
operator|)
operator|+
name|type
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|id
operator|.
name|length
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|hash
operator|=
operator|(
operator|(
name|hash
operator|<<
literal|5
operator|)
operator|+
name|hash
operator|)
operator|+
name|id
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
return|return
operator|(
name|int
operator|)
name|hash
return|;
block|}
block|}
end_class

end_unit

