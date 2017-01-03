begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.rest.yaml
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|rest
operator|.
name|yaml
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|NamedXContentRegistry
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContent
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentParser
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

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
name|List
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
comment|/**  * Holds an object and allows to extract specific values from it given their path  */
end_comment

begin_class
DECL|class|ObjectPath
specifier|public
class|class
name|ObjectPath
block|{
DECL|field|object
specifier|private
specifier|final
name|Object
name|object
decl_stmt|;
DECL|method|createFromXContent
specifier|public
specifier|static
name|ObjectPath
name|createFromXContent
parameter_list|(
name|XContent
name|xContent
parameter_list|,
name|String
name|input
parameter_list|)
throws|throws
name|IOException
block|{
try|try
init|(
name|XContentParser
name|parser
init|=
name|xContent
operator|.
name|createParser
argument_list|(
name|NamedXContentRegistry
operator|.
name|EMPTY
argument_list|,
name|input
argument_list|)
init|)
block|{
if|if
condition|(
name|parser
operator|.
name|nextToken
argument_list|()
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_ARRAY
condition|)
block|{
return|return
operator|new
name|ObjectPath
argument_list|(
name|parser
operator|.
name|listOrderedMap
argument_list|()
argument_list|)
return|;
block|}
return|return
operator|new
name|ObjectPath
argument_list|(
name|parser
operator|.
name|mapOrdered
argument_list|()
argument_list|)
return|;
block|}
block|}
DECL|method|ObjectPath
specifier|public
name|ObjectPath
parameter_list|(
name|Object
name|object
parameter_list|)
block|{
name|this
operator|.
name|object
operator|=
name|object
expr_stmt|;
block|}
comment|/**      * A utility method that creates an {@link ObjectPath} via {@link #ObjectPath(Object)} returns      * the result of calling {@link #evaluate(String)} on it.      */
DECL|method|evaluate
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|T
name|evaluate
parameter_list|(
name|Object
name|object
parameter_list|,
name|String
name|path
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|ObjectPath
argument_list|(
name|object
argument_list|)
operator|.
name|evaluate
argument_list|(
name|path
argument_list|,
name|Stash
operator|.
name|EMPTY
argument_list|)
return|;
block|}
comment|/**      * Returns the object corresponding to the provided path if present, null otherwise      */
DECL|method|evaluate
specifier|public
parameter_list|<
name|T
parameter_list|>
name|T
name|evaluate
parameter_list|(
name|String
name|path
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|evaluate
argument_list|(
name|path
argument_list|,
name|Stash
operator|.
name|EMPTY
argument_list|)
return|;
block|}
comment|/**      * Returns the object corresponding to the provided path if present, null otherwise      */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|evaluate
specifier|public
parameter_list|<
name|T
parameter_list|>
name|T
name|evaluate
parameter_list|(
name|String
name|path
parameter_list|,
name|Stash
name|stash
parameter_list|)
throws|throws
name|IOException
block|{
name|String
index|[]
name|parts
init|=
name|parsePath
argument_list|(
name|path
argument_list|)
decl_stmt|;
name|Object
name|object
init|=
name|this
operator|.
name|object
decl_stmt|;
for|for
control|(
name|String
name|part
range|:
name|parts
control|)
block|{
name|object
operator|=
name|evaluate
argument_list|(
name|part
argument_list|,
name|object
argument_list|,
name|stash
argument_list|)
expr_stmt|;
if|if
condition|(
name|object
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
block|}
return|return
operator|(
name|T
operator|)
name|object
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|evaluate
specifier|private
name|Object
name|evaluate
parameter_list|(
name|String
name|key
parameter_list|,
name|Object
name|object
parameter_list|,
name|Stash
name|stash
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|stash
operator|.
name|containsStashedValue
argument_list|(
name|key
argument_list|)
condition|)
block|{
name|key
operator|=
name|stash
operator|.
name|getValue
argument_list|(
name|key
argument_list|)
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|object
operator|instanceof
name|Map
condition|)
block|{
return|return
operator|(
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|object
operator|)
operator|.
name|get
argument_list|(
name|key
argument_list|)
return|;
block|}
if|if
condition|(
name|object
operator|instanceof
name|List
condition|)
block|{
name|List
argument_list|<
name|Object
argument_list|>
name|list
init|=
operator|(
name|List
argument_list|<
name|Object
argument_list|>
operator|)
name|object
decl_stmt|;
try|try
block|{
return|return
name|list
operator|.
name|get
argument_list|(
name|Integer
operator|.
name|valueOf
argument_list|(
name|key
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"element was a list, but ["
operator|+
name|key
operator|+
literal|"] was not numeric"
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|IndexOutOfBoundsException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"element was a list with "
operator|+
name|list
operator|.
name|size
argument_list|()
operator|+
literal|" elements, but ["
operator|+
name|key
operator|+
literal|"] was out of bounds"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"no object found for ["
operator|+
name|key
operator|+
literal|"] within object of class ["
operator|+
name|object
operator|.
name|getClass
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
DECL|method|parsePath
specifier|private
name|String
index|[]
name|parsePath
parameter_list|(
name|String
name|path
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|StringBuilder
name|current
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|boolean
name|escape
init|=
literal|false
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
name|path
operator|.
name|length
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|char
name|c
init|=
name|path
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|c
operator|==
literal|'\\'
condition|)
block|{
name|escape
operator|=
literal|true
expr_stmt|;
continue|continue;
block|}
if|if
condition|(
name|c
operator|==
literal|'.'
condition|)
block|{
if|if
condition|(
name|escape
condition|)
block|{
name|escape
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|current
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|list
operator|.
name|add
argument_list|(
name|current
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|current
operator|.
name|setLength
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
continue|continue;
block|}
block|}
name|current
operator|.
name|append
argument_list|(
name|c
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|current
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|list
operator|.
name|add
argument_list|(
name|current
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|list
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|list
operator|.
name|size
argument_list|()
index|]
argument_list|)
return|;
block|}
block|}
end_class

end_unit

