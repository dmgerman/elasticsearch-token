begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.collect
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|collect
package|;
end_package

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|Tuple
specifier|public
class|class
name|Tuple
parameter_list|<
name|V1
parameter_list|,
name|V2
parameter_list|>
block|{
DECL|method|tuple
specifier|public
specifier|static
parameter_list|<
name|V1
parameter_list|,
name|V2
parameter_list|>
name|Tuple
argument_list|<
name|V1
argument_list|,
name|V2
argument_list|>
name|tuple
parameter_list|(
name|V1
name|v1
parameter_list|,
name|V2
name|v2
parameter_list|)
block|{
return|return
operator|new
name|Tuple
argument_list|<>
argument_list|(
name|v1
argument_list|,
name|v2
argument_list|)
return|;
block|}
DECL|field|v1
specifier|private
specifier|final
name|V1
name|v1
decl_stmt|;
DECL|field|v2
specifier|private
specifier|final
name|V2
name|v2
decl_stmt|;
DECL|method|Tuple
specifier|public
name|Tuple
parameter_list|(
name|V1
name|v1
parameter_list|,
name|V2
name|v2
parameter_list|)
block|{
name|this
operator|.
name|v1
operator|=
name|v1
expr_stmt|;
name|this
operator|.
name|v2
operator|=
name|v2
expr_stmt|;
block|}
DECL|method|v1
specifier|public
name|V1
name|v1
parameter_list|()
block|{
return|return
name|v1
return|;
block|}
DECL|method|v2
specifier|public
name|V2
name|v2
parameter_list|()
block|{
return|return
name|v2
return|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
return|return
literal|true
return|;
if|if
condition|(
name|o
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|o
operator|.
name|getClass
argument_list|()
condition|)
return|return
literal|false
return|;
name|Tuple
name|tuple
init|=
operator|(
name|Tuple
operator|)
name|o
decl_stmt|;
if|if
condition|(
name|v1
operator|!=
literal|null
condition|?
operator|!
name|v1
operator|.
name|equals
argument_list|(
name|tuple
operator|.
name|v1
argument_list|)
else|:
name|tuple
operator|.
name|v1
operator|!=
literal|null
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|v2
operator|!=
literal|null
condition|?
operator|!
name|v2
operator|.
name|equals
argument_list|(
name|tuple
operator|.
name|v2
argument_list|)
else|:
name|tuple
operator|.
name|v2
operator|!=
literal|null
condition|)
return|return
literal|false
return|;
return|return
literal|true
return|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|result
init|=
name|v1
operator|!=
literal|null
condition|?
name|v1
operator|.
name|hashCode
argument_list|()
else|:
literal|0
decl_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
operator|(
name|v2
operator|!=
literal|null
condition|?
name|v2
operator|.
name|hashCode
argument_list|()
else|:
literal|0
operator|)
expr_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"Tuple [v1="
operator|+
name|v1
operator|+
literal|", v2="
operator|+
name|v2
operator|+
literal|"]"
return|;
block|}
block|}
end_class

end_unit

