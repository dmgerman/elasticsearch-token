begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright (C) 2006 Google Inc.  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.inject.internal
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|inject
operator|.
name|internal
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_comment
comment|/**  * Helps with {@code toString()} methods.  *  * @author crazybob@google.com (Bob Lee)  */
end_comment

begin_class
DECL|class|ToStringBuilder
specifier|public
class|class
name|ToStringBuilder
block|{
comment|// Linked hash map ensures ordering.
DECL|field|map
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|map
init|=
operator|new
name|LinkedHashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|name
specifier|final
name|String
name|name
decl_stmt|;
DECL|method|ToStringBuilder
specifier|public
name|ToStringBuilder
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
DECL|method|ToStringBuilder
specifier|public
name|ToStringBuilder
parameter_list|(
name|Class
name|type
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|type
operator|.
name|getSimpleName
argument_list|()
expr_stmt|;
block|}
DECL|method|add
specifier|public
name|ToStringBuilder
name|add
parameter_list|(
name|String
name|name
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
if|if
condition|(
name|map
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|value
argument_list|)
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Duplicate names: "
operator|+
name|name
argument_list|)
throw|;
block|}
return|return
name|this
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
name|name
operator|+
name|map
operator|.
name|toString
argument_list|()
operator|.
name|replace
argument_list|(
literal|'{'
argument_list|,
literal|'['
argument_list|)
operator|.
name|replace
argument_list|(
literal|'}'
argument_list|,
literal|']'
argument_list|)
return|;
block|}
block|}
end_class

end_unit

