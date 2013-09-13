begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.transport
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|transport
package|;
end_package

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|IntArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|StringTokenizer
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|PortsRange
specifier|public
class|class
name|PortsRange
block|{
DECL|field|portRange
specifier|private
specifier|final
name|String
name|portRange
decl_stmt|;
DECL|method|PortsRange
specifier|public
name|PortsRange
parameter_list|(
name|String
name|portRange
parameter_list|)
block|{
name|this
operator|.
name|portRange
operator|=
name|portRange
expr_stmt|;
block|}
DECL|method|ports
specifier|public
name|int
index|[]
name|ports
parameter_list|()
throws|throws
name|NumberFormatException
block|{
specifier|final
name|IntArrayList
name|ports
init|=
operator|new
name|IntArrayList
argument_list|()
decl_stmt|;
name|iterate
argument_list|(
operator|new
name|PortCallback
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|onPortNumber
parameter_list|(
name|int
name|portNumber
parameter_list|)
block|{
name|ports
operator|.
name|add
argument_list|(
name|portNumber
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
argument_list|)
expr_stmt|;
return|return
name|ports
operator|.
name|toArray
argument_list|()
return|;
block|}
DECL|method|iterate
specifier|public
name|boolean
name|iterate
parameter_list|(
name|PortCallback
name|callback
parameter_list|)
throws|throws
name|NumberFormatException
block|{
name|StringTokenizer
name|st
init|=
operator|new
name|StringTokenizer
argument_list|(
name|portRange
argument_list|,
literal|","
argument_list|)
decl_stmt|;
name|boolean
name|success
init|=
literal|false
decl_stmt|;
while|while
condition|(
name|st
operator|.
name|hasMoreTokens
argument_list|()
operator|&&
operator|!
name|success
condition|)
block|{
name|String
name|portToken
init|=
name|st
operator|.
name|nextToken
argument_list|()
operator|.
name|trim
argument_list|()
decl_stmt|;
name|int
name|index
init|=
name|portToken
operator|.
name|indexOf
argument_list|(
literal|'-'
argument_list|)
decl_stmt|;
if|if
condition|(
name|index
operator|==
operator|-
literal|1
condition|)
block|{
name|int
name|portNumber
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|portToken
operator|.
name|trim
argument_list|()
argument_list|)
decl_stmt|;
name|success
operator|=
name|callback
operator|.
name|onPortNumber
argument_list|(
name|portNumber
argument_list|)
expr_stmt|;
if|if
condition|(
name|success
condition|)
block|{
break|break;
block|}
block|}
else|else
block|{
name|int
name|startPort
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|portToken
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|index
argument_list|)
operator|.
name|trim
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|endPort
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|portToken
operator|.
name|substring
argument_list|(
name|index
operator|+
literal|1
argument_list|)
operator|.
name|trim
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|endPort
operator|<
name|startPort
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Start port ["
operator|+
name|startPort
operator|+
literal|"] must be greater than end port ["
operator|+
name|endPort
operator|+
literal|"]"
argument_list|)
throw|;
block|}
for|for
control|(
name|int
name|i
init|=
name|startPort
init|;
name|i
operator|<=
name|endPort
condition|;
name|i
operator|++
control|)
block|{
name|success
operator|=
name|callback
operator|.
name|onPortNumber
argument_list|(
name|i
argument_list|)
expr_stmt|;
if|if
condition|(
name|success
condition|)
block|{
break|break;
block|}
block|}
block|}
block|}
return|return
name|success
return|;
block|}
DECL|interface|PortCallback
specifier|public
specifier|static
interface|interface
name|PortCallback
block|{
DECL|method|onPortNumber
name|boolean
name|onPortNumber
parameter_list|(
name|int
name|portNumber
parameter_list|)
function_decl|;
block|}
block|}
end_class

end_unit

