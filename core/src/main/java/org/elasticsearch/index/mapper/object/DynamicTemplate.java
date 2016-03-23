begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper.object
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|object
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|Version
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
name|Strings
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
name|regex
operator|.
name|Regex
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
name|ToXContent
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
name|XContentBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|ContentPath
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|MapperParsingException
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
name|HashMap
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|DynamicTemplate
specifier|public
class|class
name|DynamicTemplate
implements|implements
name|ToXContent
block|{
DECL|enum|MatchType
specifier|public
specifier|static
enum|enum
name|MatchType
block|{
DECL|enum constant|SIMPLE
name|SIMPLE
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|matches
parameter_list|(
name|String
name|pattern
parameter_list|,
name|String
name|value
parameter_list|)
block|{
return|return
name|Regex
operator|.
name|simpleMatch
argument_list|(
name|pattern
argument_list|,
name|value
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"simple"
return|;
block|}
block|}
block|,
DECL|enum constant|REGEX
name|REGEX
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|matches
parameter_list|(
name|String
name|pattern
parameter_list|,
name|String
name|value
parameter_list|)
block|{
return|return
name|value
operator|.
name|matches
argument_list|(
name|pattern
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"regex"
return|;
block|}
block|}
block|;
DECL|method|fromString
specifier|public
specifier|static
name|MatchType
name|fromString
parameter_list|(
name|String
name|value
parameter_list|)
block|{
for|for
control|(
name|MatchType
name|v
range|:
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|v
operator|.
name|toString
argument_list|()
operator|.
name|equals
argument_list|(
name|value
argument_list|)
condition|)
block|{
return|return
name|v
return|;
block|}
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"No matching pattern matched on ["
operator|+
name|value
operator|+
literal|"]"
argument_list|)
throw|;
block|}
comment|/** Whether {@code value} matches {@code regex}. */
DECL|method|matches
specifier|public
specifier|abstract
name|boolean
name|matches
parameter_list|(
name|String
name|regex
parameter_list|,
name|String
name|value
parameter_list|)
function_decl|;
block|}
DECL|method|parse
specifier|public
specifier|static
name|DynamicTemplate
name|parse
parameter_list|(
name|String
name|name
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|conf
parameter_list|,
name|Version
name|indexVersionCreated
parameter_list|)
throws|throws
name|MapperParsingException
block|{
name|String
name|match
init|=
literal|null
decl_stmt|;
name|String
name|pathMatch
init|=
literal|null
decl_stmt|;
name|String
name|unmatch
init|=
literal|null
decl_stmt|;
name|String
name|pathUnmatch
init|=
literal|null
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|mapping
init|=
literal|null
decl_stmt|;
name|String
name|matchMappingType
init|=
literal|null
decl_stmt|;
name|String
name|matchPattern
init|=
name|MatchType
operator|.
name|SIMPLE
operator|.
name|toString
argument_list|()
decl_stmt|;
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
name|conf
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|propName
init|=
name|Strings
operator|.
name|toUnderscoreCase
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
literal|"match"
operator|.
name|equals
argument_list|(
name|propName
argument_list|)
condition|)
block|{
name|match
operator|=
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"path_match"
operator|.
name|equals
argument_list|(
name|propName
argument_list|)
condition|)
block|{
name|pathMatch
operator|=
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"unmatch"
operator|.
name|equals
argument_list|(
name|propName
argument_list|)
condition|)
block|{
name|unmatch
operator|=
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"path_unmatch"
operator|.
name|equals
argument_list|(
name|propName
argument_list|)
condition|)
block|{
name|pathUnmatch
operator|=
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"match_mapping_type"
operator|.
name|equals
argument_list|(
name|propName
argument_list|)
condition|)
block|{
name|matchMappingType
operator|=
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"match_pattern"
operator|.
name|equals
argument_list|(
name|propName
argument_list|)
condition|)
block|{
name|matchPattern
operator|=
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"mapping"
operator|.
name|equals
argument_list|(
name|propName
argument_list|)
condition|)
block|{
name|mapping
operator|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|entry
operator|.
name|getValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|indexVersionCreated
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_5_0_0
argument_list|)
condition|)
block|{
comment|// unknown parameters were ignored before but still carried through serialization
comment|// so we need to ignore them at parsing time for old indices
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Illegal dynamic template parameter: ["
operator|+
name|propName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
return|return
operator|new
name|DynamicTemplate
argument_list|(
name|name
argument_list|,
name|pathMatch
argument_list|,
name|pathUnmatch
argument_list|,
name|match
argument_list|,
name|unmatch
argument_list|,
name|matchMappingType
argument_list|,
name|MatchType
operator|.
name|fromString
argument_list|(
name|matchPattern
argument_list|)
argument_list|,
name|mapping
argument_list|)
return|;
block|}
DECL|field|name
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
DECL|field|pathMatch
specifier|private
specifier|final
name|String
name|pathMatch
decl_stmt|;
DECL|field|pathUnmatch
specifier|private
specifier|final
name|String
name|pathUnmatch
decl_stmt|;
DECL|field|match
specifier|private
specifier|final
name|String
name|match
decl_stmt|;
DECL|field|unmatch
specifier|private
specifier|final
name|String
name|unmatch
decl_stmt|;
DECL|field|matchType
specifier|private
specifier|final
name|MatchType
name|matchType
decl_stmt|;
DECL|field|matchMappingType
specifier|private
specifier|final
name|String
name|matchMappingType
decl_stmt|;
DECL|field|mapping
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|mapping
decl_stmt|;
DECL|method|DynamicTemplate
specifier|public
name|DynamicTemplate
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|pathMatch
parameter_list|,
name|String
name|pathUnmatch
parameter_list|,
name|String
name|match
parameter_list|,
name|String
name|unmatch
parameter_list|,
name|String
name|matchMappingType
parameter_list|,
name|MatchType
name|matchType
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|mapping
parameter_list|)
block|{
if|if
condition|(
name|match
operator|==
literal|null
operator|&&
name|pathMatch
operator|==
literal|null
operator|&&
name|matchMappingType
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|MapperParsingException
argument_list|(
literal|"template must have match, path_match or match_mapping_type set"
argument_list|)
throw|;
block|}
if|if
condition|(
name|mapping
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|MapperParsingException
argument_list|(
literal|"template must have mapping set"
argument_list|)
throw|;
block|}
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|pathMatch
operator|=
name|pathMatch
expr_stmt|;
name|this
operator|.
name|pathUnmatch
operator|=
name|pathUnmatch
expr_stmt|;
name|this
operator|.
name|match
operator|=
name|match
expr_stmt|;
name|this
operator|.
name|unmatch
operator|=
name|unmatch
expr_stmt|;
name|this
operator|.
name|matchType
operator|=
name|matchType
expr_stmt|;
name|this
operator|.
name|matchMappingType
operator|=
name|matchMappingType
expr_stmt|;
name|this
operator|.
name|mapping
operator|=
name|mapping
expr_stmt|;
block|}
DECL|method|name
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
name|this
operator|.
name|name
return|;
block|}
DECL|method|match
specifier|public
name|boolean
name|match
parameter_list|(
name|ContentPath
name|path
parameter_list|,
name|String
name|name
parameter_list|,
name|String
name|dynamicType
parameter_list|)
block|{
if|if
condition|(
name|pathMatch
operator|!=
literal|null
operator|&&
operator|!
name|matchType
operator|.
name|matches
argument_list|(
name|pathMatch
argument_list|,
name|path
operator|.
name|pathAsText
argument_list|(
name|name
argument_list|)
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|match
operator|!=
literal|null
operator|&&
operator|!
name|matchType
operator|.
name|matches
argument_list|(
name|match
argument_list|,
name|name
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|pathUnmatch
operator|!=
literal|null
operator|&&
name|matchType
operator|.
name|matches
argument_list|(
name|pathUnmatch
argument_list|,
name|path
operator|.
name|pathAsText
argument_list|(
name|name
argument_list|)
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|unmatch
operator|!=
literal|null
operator|&&
name|matchType
operator|.
name|matches
argument_list|(
name|unmatch
argument_list|,
name|name
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|matchMappingType
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|dynamicType
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|!
name|matchType
operator|.
name|matches
argument_list|(
name|matchMappingType
argument_list|,
name|dynamicType
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
DECL|method|mappingType
specifier|public
name|String
name|mappingType
parameter_list|(
name|String
name|dynamicType
parameter_list|)
block|{
name|String
name|type
decl_stmt|;
if|if
condition|(
name|mapping
operator|.
name|containsKey
argument_list|(
literal|"type"
argument_list|)
condition|)
block|{
name|type
operator|=
name|mapping
operator|.
name|get
argument_list|(
literal|"type"
argument_list|)
operator|.
name|toString
argument_list|()
expr_stmt|;
name|type
operator|=
name|type
operator|.
name|replace
argument_list|(
literal|"{dynamic_type}"
argument_list|,
name|dynamicType
argument_list|)
expr_stmt|;
name|type
operator|=
name|type
operator|.
name|replace
argument_list|(
literal|"{dynamicType}"
argument_list|,
name|dynamicType
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|type
operator|=
name|dynamicType
expr_stmt|;
block|}
if|if
condition|(
name|type
operator|.
name|equals
argument_list|(
name|mapping
operator|.
name|get
argument_list|(
literal|"type"
argument_list|)
argument_list|)
operator|==
literal|false
comment|// either the type was not set, or we updated it through replacements
operator|&&
literal|"text"
operator|.
name|equals
argument_list|(
name|type
argument_list|)
condition|)
block|{
comment|// and the result is "text"
comment|// now that string has been splitted into text and keyword, we use text for
comment|// dynamic mappings. However before it used to be possible to index as a keyword
comment|// by setting index=not_analyzed, so for now we will use a keyword field rather
comment|// than a text field if index=not_analyzed and the field type was not specified
comment|// explicitly
comment|// TODO: remove this in 6.0
comment|// TODO: how to do it in the future?
specifier|final
name|Object
name|index
init|=
name|mapping
operator|.
name|get
argument_list|(
literal|"index"
argument_list|)
decl_stmt|;
if|if
condition|(
literal|"not_analyzed"
operator|.
name|equals
argument_list|(
name|index
argument_list|)
operator|||
literal|"no"
operator|.
name|equals
argument_list|(
name|index
argument_list|)
condition|)
block|{
name|type
operator|=
literal|"keyword"
expr_stmt|;
block|}
block|}
return|return
name|type
return|;
block|}
DECL|method|mappingForName
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|mappingForName
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|dynamicType
parameter_list|)
block|{
return|return
name|processMap
argument_list|(
name|mapping
argument_list|,
name|name
argument_list|,
name|dynamicType
argument_list|)
return|;
block|}
DECL|method|processMap
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|processMap
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|map
parameter_list|,
name|String
name|name
parameter_list|,
name|String
name|dynamicType
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|processedMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
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
name|map
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|key
init|=
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|replace
argument_list|(
literal|"{name}"
argument_list|,
name|name
argument_list|)
operator|.
name|replace
argument_list|(
literal|"{dynamic_type}"
argument_list|,
name|dynamicType
argument_list|)
operator|.
name|replace
argument_list|(
literal|"{dynamicType}"
argument_list|,
name|dynamicType
argument_list|)
decl_stmt|;
name|Object
name|value
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|value
operator|instanceof
name|Map
condition|)
block|{
name|value
operator|=
name|processMap
argument_list|(
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|value
argument_list|,
name|name
argument_list|,
name|dynamicType
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|value
operator|instanceof
name|List
condition|)
block|{
name|value
operator|=
name|processList
argument_list|(
operator|(
name|List
operator|)
name|value
argument_list|,
name|name
argument_list|,
name|dynamicType
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|value
operator|instanceof
name|String
condition|)
block|{
name|value
operator|=
name|value
operator|.
name|toString
argument_list|()
operator|.
name|replace
argument_list|(
literal|"{name}"
argument_list|,
name|name
argument_list|)
operator|.
name|replace
argument_list|(
literal|"{dynamic_type}"
argument_list|,
name|dynamicType
argument_list|)
operator|.
name|replace
argument_list|(
literal|"{dynamicType}"
argument_list|,
name|dynamicType
argument_list|)
expr_stmt|;
block|}
name|processedMap
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
return|return
name|processedMap
return|;
block|}
DECL|method|processList
specifier|private
name|List
name|processList
parameter_list|(
name|List
name|list
parameter_list|,
name|String
name|name
parameter_list|,
name|String
name|dynamicType
parameter_list|)
block|{
name|List
name|processedList
init|=
operator|new
name|ArrayList
argument_list|()
decl_stmt|;
for|for
control|(
name|Object
name|value
range|:
name|list
control|)
block|{
if|if
condition|(
name|value
operator|instanceof
name|Map
condition|)
block|{
name|value
operator|=
name|processMap
argument_list|(
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|value
argument_list|,
name|name
argument_list|,
name|dynamicType
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|value
operator|instanceof
name|List
condition|)
block|{
name|value
operator|=
name|processList
argument_list|(
operator|(
name|List
operator|)
name|value
argument_list|,
name|name
argument_list|,
name|dynamicType
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|value
operator|instanceof
name|String
condition|)
block|{
name|value
operator|=
name|value
operator|.
name|toString
argument_list|()
operator|.
name|replace
argument_list|(
literal|"{name}"
argument_list|,
name|name
argument_list|)
operator|.
name|replace
argument_list|(
literal|"{dynamic_type}"
argument_list|,
name|dynamicType
argument_list|)
operator|.
name|replace
argument_list|(
literal|"{dynamicType}"
argument_list|,
name|dynamicType
argument_list|)
expr_stmt|;
block|}
name|processedList
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
return|return
name|processedList
return|;
block|}
annotation|@
name|Override
DECL|method|toXContent
specifier|public
name|XContentBuilder
name|toXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
if|if
condition|(
name|match
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"match"
argument_list|,
name|match
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|pathMatch
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"path_match"
argument_list|,
name|pathMatch
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|unmatch
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"unmatch"
argument_list|,
name|unmatch
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|pathUnmatch
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"path_unmatch"
argument_list|,
name|pathUnmatch
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|matchMappingType
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"match_mapping_type"
argument_list|,
name|matchMappingType
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|matchType
operator|!=
name|MatchType
operator|.
name|SIMPLE
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"match_pattern"
argument_list|,
name|matchType
argument_list|)
expr_stmt|;
block|}
comment|// use a sorted map for consistent serialization
name|builder
operator|.
name|field
argument_list|(
literal|"mapping"
argument_list|,
operator|new
name|TreeMap
argument_list|<>
argument_list|(
name|mapping
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
block|}
end_class

end_unit

