begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.rest.client
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|rest
operator|.
name|client
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|*
import|;
end_import

begin_class
DECL|class|RestPath
specifier|public
class|class
name|RestPath
block|{
DECL|field|parts
specifier|private
specifier|final
name|List
argument_list|<
name|PathPart
argument_list|>
name|parts
decl_stmt|;
DECL|field|placeholders
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|placeholders
decl_stmt|;
DECL|method|RestPath
specifier|public
name|RestPath
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|parts
parameter_list|)
block|{
name|List
argument_list|<
name|PathPart
argument_list|>
name|pathParts
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|parts
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|part
range|:
name|parts
control|)
block|{
name|pathParts
operator|.
name|add
argument_list|(
operator|new
name|PathPart
argument_list|(
name|part
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|parts
operator|=
name|pathParts
expr_stmt|;
name|this
operator|.
name|placeholders
operator|=
name|Collections
operator|.
name|emptyList
argument_list|()
expr_stmt|;
block|}
DECL|method|RestPath
specifier|public
name|RestPath
parameter_list|(
name|String
name|path
parameter_list|)
block|{
name|String
index|[]
name|pathParts
init|=
name|path
operator|.
name|split
argument_list|(
literal|"/"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|placeholders
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|PathPart
argument_list|>
name|parts
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|pathPart
range|:
name|pathParts
control|)
block|{
if|if
condition|(
name|pathPart
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
if|if
condition|(
name|pathPart
operator|.
name|startsWith
argument_list|(
literal|"{"
argument_list|)
condition|)
block|{
if|if
condition|(
name|pathPart
operator|.
name|indexOf
argument_list|(
literal|'}'
argument_list|)
operator|!=
name|pathPart
operator|.
name|length
argument_list|()
operator|-
literal|1
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"more than one parameter found in the same path part: ["
operator|+
name|pathPart
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|String
name|placeholder
init|=
name|pathPart
operator|.
name|substring
argument_list|(
literal|1
argument_list|,
name|pathPart
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
decl_stmt|;
name|parts
operator|.
name|add
argument_list|(
operator|new
name|PathPart
argument_list|(
name|placeholder
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|placeholders
operator|.
name|add
argument_list|(
name|placeholder
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|parts
operator|.
name|add
argument_list|(
operator|new
name|PathPart
argument_list|(
name|pathPart
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|this
operator|.
name|placeholders
operator|=
name|placeholders
expr_stmt|;
name|this
operator|.
name|parts
operator|=
name|parts
expr_stmt|;
block|}
DECL|method|getPathParts
specifier|public
name|String
index|[]
name|getPathParts
parameter_list|()
block|{
name|String
index|[]
name|parts
init|=
operator|new
name|String
index|[
name|this
operator|.
name|parts
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|PathPart
name|part
range|:
name|this
operator|.
name|parts
control|)
block|{
name|parts
index|[
name|i
operator|++
index|]
operator|=
name|part
operator|.
name|pathPart
expr_stmt|;
block|}
return|return
name|parts
return|;
block|}
DECL|method|matches
specifier|public
name|boolean
name|matches
parameter_list|(
name|Set
argument_list|<
name|String
argument_list|>
name|params
parameter_list|)
block|{
return|return
name|placeholders
operator|.
name|size
argument_list|()
operator|==
name|params
operator|.
name|size
argument_list|()
operator|&&
name|placeholders
operator|.
name|containsAll
argument_list|(
name|params
argument_list|)
return|;
block|}
DECL|method|replacePlaceholders
specifier|public
name|RestPath
name|replacePlaceholders
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|finalPathParts
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|parts
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|PathPart
name|pathPart
range|:
name|parts
control|)
block|{
if|if
condition|(
name|pathPart
operator|.
name|isPlaceholder
condition|)
block|{
name|String
name|value
init|=
name|params
operator|.
name|get
argument_list|(
name|pathPart
operator|.
name|pathPart
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"parameter ["
operator|+
name|pathPart
operator|.
name|pathPart
operator|+
literal|"] missing"
argument_list|)
throw|;
block|}
name|finalPathParts
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|finalPathParts
operator|.
name|add
argument_list|(
name|pathPart
operator|.
name|pathPart
argument_list|)
expr_stmt|;
block|}
block|}
return|return
operator|new
name|RestPath
argument_list|(
name|finalPathParts
argument_list|)
return|;
block|}
DECL|class|PathPart
specifier|private
specifier|static
class|class
name|PathPart
block|{
DECL|field|isPlaceholder
specifier|private
specifier|final
name|boolean
name|isPlaceholder
decl_stmt|;
DECL|field|pathPart
specifier|private
specifier|final
name|String
name|pathPart
decl_stmt|;
DECL|method|PathPart
specifier|private
name|PathPart
parameter_list|(
name|String
name|pathPart
parameter_list|,
name|boolean
name|isPlaceholder
parameter_list|)
block|{
name|this
operator|.
name|isPlaceholder
operator|=
name|isPlaceholder
expr_stmt|;
name|this
operator|.
name|pathPart
operator|=
name|pathPart
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

