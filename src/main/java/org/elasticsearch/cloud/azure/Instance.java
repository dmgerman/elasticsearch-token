begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cloud.azure
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cloud
operator|.
name|azure
package|;
end_package

begin_comment
comment|/**  * Define an Azure Instance  */
end_comment

begin_class
DECL|class|Instance
specifier|public
class|class
name|Instance
block|{
DECL|enum|Status
specifier|public
specifier|static
enum|enum
name|Status
block|{
DECL|enum constant|STARTED
name|STARTED
block|;     }
DECL|field|privateIp
specifier|private
name|String
name|privateIp
decl_stmt|;
DECL|field|publicIp
specifier|private
name|String
name|publicIp
decl_stmt|;
DECL|field|publicPort
specifier|private
name|String
name|publicPort
decl_stmt|;
DECL|field|status
specifier|private
name|Status
name|status
decl_stmt|;
DECL|field|name
specifier|private
name|String
name|name
decl_stmt|;
DECL|method|getPrivateIp
specifier|public
name|String
name|getPrivateIp
parameter_list|()
block|{
return|return
name|privateIp
return|;
block|}
DECL|method|setPrivateIp
specifier|public
name|void
name|setPrivateIp
parameter_list|(
name|String
name|privateIp
parameter_list|)
block|{
name|this
operator|.
name|privateIp
operator|=
name|privateIp
expr_stmt|;
block|}
DECL|method|getStatus
specifier|public
name|Status
name|getStatus
parameter_list|()
block|{
return|return
name|status
return|;
block|}
DECL|method|setStatus
specifier|public
name|void
name|setStatus
parameter_list|(
name|Status
name|status
parameter_list|)
block|{
name|this
operator|.
name|status
operator|=
name|status
expr_stmt|;
block|}
DECL|method|getName
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
DECL|method|setName
specifier|public
name|void
name|setName
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
DECL|method|getPublicIp
specifier|public
name|String
name|getPublicIp
parameter_list|()
block|{
return|return
name|publicIp
return|;
block|}
DECL|method|setPublicIp
specifier|public
name|void
name|setPublicIp
parameter_list|(
name|String
name|publicIp
parameter_list|)
block|{
name|this
operator|.
name|publicIp
operator|=
name|publicIp
expr_stmt|;
block|}
DECL|method|getPublicPort
specifier|public
name|String
name|getPublicPort
parameter_list|()
block|{
return|return
name|publicPort
return|;
block|}
DECL|method|setPublicPort
specifier|public
name|void
name|setPublicPort
parameter_list|(
name|String
name|publicPort
parameter_list|)
block|{
name|this
operator|.
name|publicPort
operator|=
name|publicPort
expr_stmt|;
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
name|Instance
name|instance
init|=
operator|(
name|Instance
operator|)
name|o
decl_stmt|;
if|if
condition|(
name|name
operator|!=
literal|null
condition|?
operator|!
name|name
operator|.
name|equals
argument_list|(
name|instance
operator|.
name|name
argument_list|)
else|:
name|instance
operator|.
name|name
operator|!=
literal|null
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|privateIp
operator|!=
literal|null
condition|?
operator|!
name|privateIp
operator|.
name|equals
argument_list|(
name|instance
operator|.
name|privateIp
argument_list|)
else|:
name|instance
operator|.
name|privateIp
operator|!=
literal|null
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|publicIp
operator|!=
literal|null
condition|?
operator|!
name|publicIp
operator|.
name|equals
argument_list|(
name|instance
operator|.
name|publicIp
argument_list|)
else|:
name|instance
operator|.
name|publicIp
operator|!=
literal|null
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|publicPort
operator|!=
literal|null
condition|?
operator|!
name|publicPort
operator|.
name|equals
argument_list|(
name|instance
operator|.
name|publicPort
argument_list|)
else|:
name|instance
operator|.
name|publicPort
operator|!=
literal|null
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|status
operator|!=
name|instance
operator|.
name|status
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
name|privateIp
operator|!=
literal|null
condition|?
name|privateIp
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
name|publicIp
operator|!=
literal|null
condition|?
name|publicIp
operator|.
name|hashCode
argument_list|()
else|:
literal|0
operator|)
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
operator|(
name|publicPort
operator|!=
literal|null
condition|?
name|publicPort
operator|.
name|hashCode
argument_list|()
else|:
literal|0
operator|)
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
operator|(
name|status
operator|!=
literal|null
condition|?
name|status
operator|.
name|hashCode
argument_list|()
else|:
literal|0
operator|)
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
operator|(
name|name
operator|!=
literal|null
condition|?
name|name
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
specifier|final
name|StringBuffer
name|sb
init|=
operator|new
name|StringBuffer
argument_list|(
literal|"Instance{"
argument_list|)
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"privateIp='"
argument_list|)
operator|.
name|append
argument_list|(
name|privateIp
argument_list|)
operator|.
name|append
argument_list|(
literal|'\''
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", publicIp='"
argument_list|)
operator|.
name|append
argument_list|(
name|publicIp
argument_list|)
operator|.
name|append
argument_list|(
literal|'\''
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", publicPort='"
argument_list|)
operator|.
name|append
argument_list|(
name|publicPort
argument_list|)
operator|.
name|append
argument_list|(
literal|'\''
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", status="
argument_list|)
operator|.
name|append
argument_list|(
name|status
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", name='"
argument_list|)
operator|.
name|append
argument_list|(
name|name
argument_list|)
operator|.
name|append
argument_list|(
literal|'\''
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|'}'
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

