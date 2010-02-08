begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
package|;
end_package

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|Uid
specifier|public
specifier|final
class|class
name|Uid
block|{
DECL|field|DELIMITER
specifier|public
specifier|static
specifier|final
name|char
name|DELIMITER
init|=
literal|'#'
decl_stmt|;
DECL|field|type
specifier|private
specifier|final
name|String
name|type
decl_stmt|;
DECL|field|id
specifier|private
specifier|final
name|String
name|id
decl_stmt|;
DECL|method|Uid
specifier|public
name|Uid
parameter_list|(
name|String
name|type
parameter_list|,
name|String
name|id
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
block|}
DECL|method|type
specifier|public
name|String
name|type
parameter_list|()
block|{
return|return
name|type
return|;
block|}
DECL|method|id
specifier|public
name|String
name|id
parameter_list|()
block|{
return|return
name|id
return|;
block|}
DECL|method|createUid
specifier|public
specifier|static
name|Uid
name|createUid
parameter_list|(
name|String
name|uid
parameter_list|)
block|{
name|int
name|delimiterIndex
init|=
name|uid
operator|.
name|lastIndexOf
argument_list|(
name|DELIMITER
argument_list|)
decl_stmt|;
return|return
operator|new
name|Uid
argument_list|(
name|uid
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|delimiterIndex
argument_list|)
argument_list|,
name|uid
operator|.
name|substring
argument_list|(
name|delimiterIndex
operator|+
literal|1
argument_list|)
argument_list|)
return|;
block|}
DECL|method|createUid
specifier|public
specifier|static
name|String
name|createUid
parameter_list|(
name|String
name|type
parameter_list|,
name|String
name|id
parameter_list|)
block|{
return|return
name|createUid
argument_list|(
operator|new
name|StringBuilder
argument_list|()
argument_list|,
name|type
argument_list|,
name|id
argument_list|)
return|;
block|}
DECL|method|createUid
specifier|public
specifier|static
name|String
name|createUid
parameter_list|(
name|StringBuilder
name|sb
parameter_list|,
name|String
name|type
parameter_list|,
name|String
name|id
parameter_list|)
block|{
return|return
name|sb
operator|.
name|append
argument_list|(
name|type
argument_list|)
operator|.
name|append
argument_list|(
name|DELIMITER
argument_list|)
operator|.
name|append
argument_list|(
name|id
argument_list|)
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

