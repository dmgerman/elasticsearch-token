begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.ingest.processor.mutate
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|processor
operator|.
name|mutate
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
name|Booleans
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|Data
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|processor
operator|.
name|ConfigurationUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|processor
operator|.
name|Processor
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
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
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
DECL|class|MutateProcessor
specifier|public
specifier|final
class|class
name|MutateProcessor
implements|implements
name|Processor
block|{
DECL|field|TYPE
specifier|public
specifier|static
specifier|final
name|String
name|TYPE
init|=
literal|"mutate"
decl_stmt|;
DECL|field|update
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|update
decl_stmt|;
DECL|field|rename
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|rename
decl_stmt|;
DECL|field|convert
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|convert
decl_stmt|;
DECL|field|split
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|split
decl_stmt|;
DECL|field|gsub
specifier|private
specifier|final
name|List
argument_list|<
name|GsubExpression
argument_list|>
name|gsub
decl_stmt|;
DECL|field|join
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|join
decl_stmt|;
DECL|field|remove
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|remove
decl_stmt|;
DECL|field|trim
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|trim
decl_stmt|;
DECL|field|uppercase
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|uppercase
decl_stmt|;
DECL|field|lowercase
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|lowercase
decl_stmt|;
DECL|method|MutateProcessor
name|MutateProcessor
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|update
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|rename
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|convert
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|split
parameter_list|,
name|List
argument_list|<
name|GsubExpression
argument_list|>
name|gsub
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|join
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|remove
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|trim
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|uppercase
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|lowercase
parameter_list|)
block|{
name|this
operator|.
name|update
operator|=
name|update
expr_stmt|;
name|this
operator|.
name|rename
operator|=
name|rename
expr_stmt|;
name|this
operator|.
name|convert
operator|=
name|convert
expr_stmt|;
name|this
operator|.
name|split
operator|=
name|split
expr_stmt|;
name|this
operator|.
name|gsub
operator|=
name|gsub
expr_stmt|;
name|this
operator|.
name|join
operator|=
name|join
expr_stmt|;
name|this
operator|.
name|remove
operator|=
name|remove
expr_stmt|;
name|this
operator|.
name|trim
operator|=
name|trim
expr_stmt|;
name|this
operator|.
name|uppercase
operator|=
name|uppercase
expr_stmt|;
name|this
operator|.
name|lowercase
operator|=
name|lowercase
expr_stmt|;
block|}
DECL|method|getUpdate
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|getUpdate
parameter_list|()
block|{
return|return
name|update
return|;
block|}
DECL|method|getRename
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getRename
parameter_list|()
block|{
return|return
name|rename
return|;
block|}
DECL|method|getConvert
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getConvert
parameter_list|()
block|{
return|return
name|convert
return|;
block|}
DECL|method|getSplit
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getSplit
parameter_list|()
block|{
return|return
name|split
return|;
block|}
DECL|method|getGsub
name|List
argument_list|<
name|GsubExpression
argument_list|>
name|getGsub
parameter_list|()
block|{
return|return
name|gsub
return|;
block|}
DECL|method|getJoin
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getJoin
parameter_list|()
block|{
return|return
name|join
return|;
block|}
DECL|method|getRemove
name|List
argument_list|<
name|String
argument_list|>
name|getRemove
parameter_list|()
block|{
return|return
name|remove
return|;
block|}
DECL|method|getTrim
name|List
argument_list|<
name|String
argument_list|>
name|getTrim
parameter_list|()
block|{
return|return
name|trim
return|;
block|}
DECL|method|getUppercase
name|List
argument_list|<
name|String
argument_list|>
name|getUppercase
parameter_list|()
block|{
return|return
name|uppercase
return|;
block|}
DECL|method|getLowercase
name|List
argument_list|<
name|String
argument_list|>
name|getLowercase
parameter_list|()
block|{
return|return
name|lowercase
return|;
block|}
annotation|@
name|Override
DECL|method|execute
specifier|public
name|void
name|execute
parameter_list|(
name|Data
name|data
parameter_list|)
block|{
if|if
condition|(
name|update
operator|!=
literal|null
condition|)
block|{
name|doUpdate
argument_list|(
name|data
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|rename
operator|!=
literal|null
condition|)
block|{
name|doRename
argument_list|(
name|data
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|convert
operator|!=
literal|null
condition|)
block|{
name|doConvert
argument_list|(
name|data
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|split
operator|!=
literal|null
condition|)
block|{
name|doSplit
argument_list|(
name|data
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|gsub
operator|!=
literal|null
condition|)
block|{
name|doGsub
argument_list|(
name|data
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|join
operator|!=
literal|null
condition|)
block|{
name|doJoin
argument_list|(
name|data
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|remove
operator|!=
literal|null
condition|)
block|{
name|doRemove
argument_list|(
name|data
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|trim
operator|!=
literal|null
condition|)
block|{
name|doTrim
argument_list|(
name|data
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|uppercase
operator|!=
literal|null
condition|)
block|{
name|doUppercase
argument_list|(
name|data
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|lowercase
operator|!=
literal|null
condition|)
block|{
name|doLowercase
argument_list|(
name|data
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|getType
specifier|public
name|String
name|getType
parameter_list|()
block|{
return|return
name|TYPE
return|;
block|}
DECL|method|doUpdate
specifier|private
name|void
name|doUpdate
parameter_list|(
name|Data
name|data
parameter_list|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|entry
range|:
name|update
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|data
operator|.
name|addField
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|doRename
specifier|private
name|void
name|doRename
parameter_list|(
name|Data
name|data
parameter_list|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|rename
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|data
operator|.
name|containsProperty
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
condition|)
block|{
name|Object
name|oldVal
init|=
name|data
operator|.
name|getProperty
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
name|data
operator|.
name|getDocument
argument_list|()
operator|.
name|remove
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|data
operator|.
name|addField
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|,
name|oldVal
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|parseValueAsType
specifier|private
name|Object
name|parseValueAsType
parameter_list|(
name|Object
name|oldVal
parameter_list|,
name|String
name|toType
parameter_list|)
block|{
switch|switch
condition|(
name|toType
condition|)
block|{
case|case
literal|"integer"
case|:
name|oldVal
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|oldVal
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
literal|"float"
case|:
name|oldVal
operator|=
name|Float
operator|.
name|parseFloat
argument_list|(
name|oldVal
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
literal|"string"
case|:
name|oldVal
operator|=
name|oldVal
operator|.
name|toString
argument_list|()
expr_stmt|;
break|break;
case|case
literal|"boolean"
case|:
comment|// TODO(talevy): Booleans#parseBoolean depends on Elasticsearch, should be moved into dedicated library.
name|oldVal
operator|=
name|Booleans
operator|.
name|parseBoolean
argument_list|(
name|oldVal
operator|.
name|toString
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
return|return
name|oldVal
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|doConvert
specifier|private
name|void
name|doConvert
parameter_list|(
name|Data
name|data
parameter_list|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|convert
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|toType
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|Object
name|oldVal
init|=
name|data
operator|.
name|getProperty
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
name|Object
name|newVal
decl_stmt|;
if|if
condition|(
name|oldVal
operator|instanceof
name|List
condition|)
block|{
name|newVal
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
for|for
control|(
name|Object
name|e
range|:
operator|(
operator|(
name|List
argument_list|<
name|Object
argument_list|>
operator|)
name|oldVal
operator|)
control|)
block|{
operator|(
operator|(
name|List
argument_list|<
name|Object
argument_list|>
operator|)
name|newVal
operator|)
operator|.
name|add
argument_list|(
name|parseValueAsType
argument_list|(
name|e
argument_list|,
name|toType
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|oldVal
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Field \""
operator|+
name|entry
operator|.
name|getKey
argument_list|()
operator|+
literal|"\" is null, cannot be converted to a/an "
operator|+
name|toType
argument_list|)
throw|;
block|}
name|newVal
operator|=
name|parseValueAsType
argument_list|(
name|oldVal
argument_list|,
name|toType
argument_list|)
expr_stmt|;
block|}
name|data
operator|.
name|addField
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|newVal
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|doSplit
specifier|private
name|void
name|doSplit
parameter_list|(
name|Data
name|data
parameter_list|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|split
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Object
name|oldVal
init|=
name|data
operator|.
name|getProperty
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|oldVal
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Cannot split field. ["
operator|+
name|entry
operator|.
name|getKey
argument_list|()
operator|+
literal|"] is null."
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
name|oldVal
operator|instanceof
name|String
condition|)
block|{
name|data
operator|.
name|addField
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
operator|(
operator|(
name|String
operator|)
name|oldVal
operator|)
operator|.
name|split
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Cannot split a field that is not a String type"
argument_list|)
throw|;
block|}
block|}
block|}
DECL|method|doGsub
specifier|private
name|void
name|doGsub
parameter_list|(
name|Data
name|data
parameter_list|)
block|{
for|for
control|(
name|GsubExpression
name|gsubExpression
range|:
name|gsub
control|)
block|{
name|String
name|oldVal
init|=
name|data
operator|.
name|getProperty
argument_list|(
name|gsubExpression
operator|.
name|getFieldName
argument_list|()
argument_list|,
name|String
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|oldVal
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Field \""
operator|+
name|gsubExpression
operator|.
name|getFieldName
argument_list|()
operator|+
literal|"\" is null, cannot match pattern."
argument_list|)
throw|;
block|}
name|Matcher
name|matcher
init|=
name|gsubExpression
operator|.
name|getPattern
argument_list|()
operator|.
name|matcher
argument_list|(
name|oldVal
argument_list|)
decl_stmt|;
name|String
name|newVal
init|=
name|matcher
operator|.
name|replaceAll
argument_list|(
name|gsubExpression
operator|.
name|getReplacement
argument_list|()
argument_list|)
decl_stmt|;
name|data
operator|.
name|addField
argument_list|(
name|gsubExpression
operator|.
name|getFieldName
argument_list|()
argument_list|,
name|newVal
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|doJoin
specifier|private
name|void
name|doJoin
parameter_list|(
name|Data
name|data
parameter_list|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|join
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Object
name|oldVal
init|=
name|data
operator|.
name|getProperty
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|oldVal
operator|instanceof
name|List
condition|)
block|{
name|String
name|joined
init|=
call|(
name|String
call|)
argument_list|(
operator|(
name|List
operator|)
name|oldVal
argument_list|)
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|Object
operator|::
name|toString
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|joining
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|data
operator|.
name|addField
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|joined
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Cannot join field:"
operator|+
name|entry
operator|.
name|getKey
argument_list|()
operator|+
literal|" with type: "
operator|+
name|oldVal
operator|.
name|getClass
argument_list|()
argument_list|)
throw|;
block|}
block|}
block|}
DECL|method|doRemove
specifier|private
name|void
name|doRemove
parameter_list|(
name|Data
name|data
parameter_list|)
block|{
for|for
control|(
name|String
name|field
range|:
name|remove
control|)
block|{
name|data
operator|.
name|getDocument
argument_list|()
operator|.
name|remove
argument_list|(
name|field
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|doTrim
specifier|private
name|void
name|doTrim
parameter_list|(
name|Data
name|data
parameter_list|)
block|{
for|for
control|(
name|String
name|field
range|:
name|trim
control|)
block|{
name|Object
name|val
init|=
name|data
operator|.
name|getProperty
argument_list|(
name|field
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|val
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Cannot trim field. ["
operator|+
name|field
operator|+
literal|"] is null."
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
name|val
operator|instanceof
name|String
condition|)
block|{
name|data
operator|.
name|addField
argument_list|(
name|field
argument_list|,
operator|(
operator|(
name|String
operator|)
name|val
operator|)
operator|.
name|trim
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Cannot trim field:"
operator|+
name|field
operator|+
literal|" with type: "
operator|+
name|val
operator|.
name|getClass
argument_list|()
argument_list|)
throw|;
block|}
block|}
block|}
DECL|method|doUppercase
specifier|private
name|void
name|doUppercase
parameter_list|(
name|Data
name|data
parameter_list|)
block|{
for|for
control|(
name|String
name|field
range|:
name|uppercase
control|)
block|{
name|Object
name|val
init|=
name|data
operator|.
name|getProperty
argument_list|(
name|field
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|val
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Cannot uppercase field. ["
operator|+
name|field
operator|+
literal|"] is null."
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
name|val
operator|instanceof
name|String
condition|)
block|{
name|data
operator|.
name|addField
argument_list|(
name|field
argument_list|,
operator|(
operator|(
name|String
operator|)
name|val
operator|)
operator|.
name|toUpperCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Cannot uppercase field:"
operator|+
name|field
operator|+
literal|" with type: "
operator|+
name|val
operator|.
name|getClass
argument_list|()
argument_list|)
throw|;
block|}
block|}
block|}
DECL|method|doLowercase
specifier|private
name|void
name|doLowercase
parameter_list|(
name|Data
name|data
parameter_list|)
block|{
for|for
control|(
name|String
name|field
range|:
name|lowercase
control|)
block|{
name|Object
name|val
init|=
name|data
operator|.
name|getProperty
argument_list|(
name|field
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|val
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Cannot lowercase field. ["
operator|+
name|field
operator|+
literal|"] is null."
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
name|val
operator|instanceof
name|String
condition|)
block|{
name|data
operator|.
name|addField
argument_list|(
name|field
argument_list|,
operator|(
operator|(
name|String
operator|)
name|val
operator|)
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Cannot lowercase field:"
operator|+
name|field
operator|+
literal|" with type: "
operator|+
name|val
operator|.
name|getClass
argument_list|()
argument_list|)
throw|;
block|}
block|}
block|}
DECL|class|Factory
specifier|public
specifier|static
specifier|final
class|class
name|Factory
implements|implements
name|Processor
operator|.
name|Factory
argument_list|<
name|MutateProcessor
argument_list|>
block|{
annotation|@
name|Override
DECL|method|create
specifier|public
name|MutateProcessor
name|create
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|config
parameter_list|)
throws|throws
name|IOException
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|update
init|=
name|ConfigurationUtils
operator|.
name|readOptionalMap
argument_list|(
name|config
argument_list|,
literal|"update"
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|rename
init|=
name|ConfigurationUtils
operator|.
name|readOptionalMap
argument_list|(
name|config
argument_list|,
literal|"rename"
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|convert
init|=
name|ConfigurationUtils
operator|.
name|readOptionalMap
argument_list|(
name|config
argument_list|,
literal|"convert"
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|split
init|=
name|ConfigurationUtils
operator|.
name|readOptionalMap
argument_list|(
name|config
argument_list|,
literal|"split"
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|gsubConfig
init|=
name|ConfigurationUtils
operator|.
name|readOptionalMap
argument_list|(
name|config
argument_list|,
literal|"gsub"
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|join
init|=
name|ConfigurationUtils
operator|.
name|readOptionalMap
argument_list|(
name|config
argument_list|,
literal|"join"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|remove
init|=
name|ConfigurationUtils
operator|.
name|readOptionalList
argument_list|(
name|config
argument_list|,
literal|"remove"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|trim
init|=
name|ConfigurationUtils
operator|.
name|readOptionalList
argument_list|(
name|config
argument_list|,
literal|"trim"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|uppercase
init|=
name|ConfigurationUtils
operator|.
name|readOptionalList
argument_list|(
name|config
argument_list|,
literal|"uppercase"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|lowercase
init|=
name|ConfigurationUtils
operator|.
name|readOptionalList
argument_list|(
name|config
argument_list|,
literal|"lowercase"
argument_list|)
decl_stmt|;
comment|// pre-compile regex patterns
name|List
argument_list|<
name|GsubExpression
argument_list|>
name|gsubExpressions
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|gsubConfig
operator|!=
literal|null
condition|)
block|{
name|gsubExpressions
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|entry
range|:
name|gsubConfig
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|searchAndReplace
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|searchAndReplace
operator|.
name|size
argument_list|()
operator|!=
literal|2
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid search and replace values "
operator|+
name|searchAndReplace
operator|+
literal|" for field: "
operator|+
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
throw|;
block|}
name|Pattern
name|searchPattern
init|=
name|Pattern
operator|.
name|compile
argument_list|(
name|searchAndReplace
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|gsubExpressions
operator|.
name|add
argument_list|(
operator|new
name|GsubExpression
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|searchPattern
argument_list|,
name|searchAndReplace
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
operator|new
name|MutateProcessor
argument_list|(
operator|(
name|update
operator|==
literal|null
operator|)
condition|?
literal|null
else|:
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|update
argument_list|)
argument_list|,
operator|(
name|rename
operator|==
literal|null
operator|)
condition|?
literal|null
else|:
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|rename
argument_list|)
argument_list|,
operator|(
name|convert
operator|==
literal|null
operator|)
condition|?
literal|null
else|:
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|convert
argument_list|)
argument_list|,
operator|(
name|split
operator|==
literal|null
operator|)
condition|?
literal|null
else|:
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|split
argument_list|)
argument_list|,
operator|(
name|gsubExpressions
operator|==
literal|null
operator|)
condition|?
literal|null
else|:
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|gsubExpressions
argument_list|)
argument_list|,
operator|(
name|join
operator|==
literal|null
operator|)
condition|?
literal|null
else|:
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|join
argument_list|)
argument_list|,
operator|(
name|remove
operator|==
literal|null
operator|)
condition|?
literal|null
else|:
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|remove
argument_list|)
argument_list|,
operator|(
name|trim
operator|==
literal|null
operator|)
condition|?
literal|null
else|:
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|trim
argument_list|)
argument_list|,
operator|(
name|uppercase
operator|==
literal|null
operator|)
condition|?
literal|null
else|:
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|uppercase
argument_list|)
argument_list|,
operator|(
name|lowercase
operator|==
literal|null
operator|)
condition|?
literal|null
else|:
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|lowercase
argument_list|)
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

