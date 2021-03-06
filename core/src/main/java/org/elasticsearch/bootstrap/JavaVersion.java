begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.bootstrap
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|bootstrap
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Objects
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
import|;
end_import

begin_class
DECL|class|JavaVersion
specifier|public
class|class
name|JavaVersion
implements|implements
name|Comparable
argument_list|<
name|JavaVersion
argument_list|>
block|{
DECL|field|version
specifier|private
specifier|final
name|List
argument_list|<
name|Integer
argument_list|>
name|version
decl_stmt|;
DECL|method|getVersion
specifier|public
name|List
argument_list|<
name|Integer
argument_list|>
name|getVersion
parameter_list|()
block|{
return|return
name|version
return|;
block|}
DECL|method|JavaVersion
specifier|private
name|JavaVersion
parameter_list|(
name|List
argument_list|<
name|Integer
argument_list|>
name|version
parameter_list|)
block|{
if|if
condition|(
name|version
operator|.
name|size
argument_list|()
operator|>=
literal|2
operator|&&
name|version
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|==
literal|1
operator|&&
name|version
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|==
literal|8
condition|)
block|{
comment|// for Java 8 there is ambiguity since both 1.8 and 8 are supported,
comment|// so we rewrite the former to the latter
name|version
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|version
operator|.
name|subList
argument_list|(
literal|1
argument_list|,
name|version
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|version
operator|=
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|version
argument_list|)
expr_stmt|;
block|}
DECL|method|parse
specifier|public
specifier|static
name|JavaVersion
name|parse
parameter_list|(
name|String
name|value
parameter_list|)
block|{
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|value
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|isValid
argument_list|(
name|value
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"value"
argument_list|)
throw|;
block|}
name|List
argument_list|<
name|Integer
argument_list|>
name|version
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|String
index|[]
name|components
init|=
name|value
operator|.
name|split
argument_list|(
literal|"\\."
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|component
range|:
name|components
control|)
block|{
name|version
operator|.
name|add
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
name|component
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|JavaVersion
argument_list|(
name|version
argument_list|)
return|;
block|}
DECL|method|isValid
specifier|public
specifier|static
name|boolean
name|isValid
parameter_list|(
name|String
name|value
parameter_list|)
block|{
return|return
name|value
operator|.
name|matches
argument_list|(
literal|"^0*[0-9]+(\\.[0-9]+)*$"
argument_list|)
return|;
block|}
DECL|field|CURRENT
specifier|private
specifier|static
specifier|final
name|JavaVersion
name|CURRENT
init|=
name|parse
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"java.specification.version"
argument_list|)
argument_list|)
decl_stmt|;
DECL|method|current
specifier|public
specifier|static
name|JavaVersion
name|current
parameter_list|()
block|{
return|return
name|CURRENT
return|;
block|}
annotation|@
name|Override
DECL|method|compareTo
specifier|public
name|int
name|compareTo
parameter_list|(
name|JavaVersion
name|o
parameter_list|)
block|{
name|int
name|len
init|=
name|Math
operator|.
name|max
argument_list|(
name|version
operator|.
name|size
argument_list|()
argument_list|,
name|o
operator|.
name|version
operator|.
name|size
argument_list|()
argument_list|)
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
name|len
condition|;
name|i
operator|++
control|)
block|{
name|int
name|d
init|=
operator|(
name|i
operator|<
name|version
operator|.
name|size
argument_list|()
condition|?
name|version
operator|.
name|get
argument_list|(
name|i
argument_list|)
else|:
literal|0
operator|)
decl_stmt|;
name|int
name|s
init|=
operator|(
name|i
operator|<
name|o
operator|.
name|version
operator|.
name|size
argument_list|()
condition|?
name|o
operator|.
name|version
operator|.
name|get
argument_list|(
name|i
argument_list|)
else|:
literal|0
operator|)
decl_stmt|;
if|if
condition|(
name|s
operator|<
name|d
condition|)
return|return
literal|1
return|;
if|if
condition|(
name|s
operator|>
name|d
condition|)
return|return
operator|-
literal|1
return|;
block|}
return|return
literal|0
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
name|o
operator|==
literal|null
operator|||
name|o
operator|.
name|getClass
argument_list|()
operator|!=
name|getClass
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
name|compareTo
argument_list|(
operator|(
name|JavaVersion
operator|)
name|o
argument_list|)
operator|==
literal|0
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
return|return
name|version
operator|.
name|hashCode
argument_list|()
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
name|version
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|v
lambda|->
name|Integer
operator|.
name|toString
argument_list|(
name|v
argument_list|)
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|joining
argument_list|(
literal|"."
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

