begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.xcontent.support
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|support
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|automaton
operator|.
name|Automata
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|automaton
operator|.
name|Automaton
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|automaton
operator|.
name|CharacterRunAutomaton
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|automaton
operator|.
name|Operations
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchParseException
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
name|Numbers
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
name|unit
operator|.
name|TimeValue
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
name|Arrays
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
name|function
operator|.
name|Function
import|;
end_import

begin_class
DECL|class|XContentMapValues
specifier|public
class|class
name|XContentMapValues
block|{
comment|/**      * Extracts raw values (string, int, and so on) based on the path provided returning all of them      * as a single list.      */
DECL|method|extractRawValues
specifier|public
specifier|static
name|List
argument_list|<
name|Object
argument_list|>
name|extractRawValues
parameter_list|(
name|String
name|path
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|map
parameter_list|)
block|{
name|List
argument_list|<
name|Object
argument_list|>
name|values
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|String
index|[]
name|pathElements
init|=
name|path
operator|.
name|split
argument_list|(
literal|"\\."
argument_list|)
decl_stmt|;
if|if
condition|(
name|pathElements
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
name|values
return|;
block|}
name|extractRawValues
argument_list|(
name|values
argument_list|,
name|map
argument_list|,
name|pathElements
argument_list|,
literal|0
argument_list|)
expr_stmt|;
return|return
name|values
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"unchecked"
block|}
argument_list|)
DECL|method|extractRawValues
specifier|private
specifier|static
name|void
name|extractRawValues
parameter_list|(
name|List
name|values
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|part
parameter_list|,
name|String
index|[]
name|pathElements
parameter_list|,
name|int
name|index
parameter_list|)
block|{
if|if
condition|(
name|index
operator|==
name|pathElements
operator|.
name|length
condition|)
block|{
return|return;
block|}
name|String
name|key
init|=
name|pathElements
index|[
name|index
index|]
decl_stmt|;
name|Object
name|currentValue
init|=
name|part
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
name|int
name|nextIndex
init|=
name|index
operator|+
literal|1
decl_stmt|;
while|while
condition|(
name|currentValue
operator|==
literal|null
operator|&&
name|nextIndex
operator|!=
name|pathElements
operator|.
name|length
condition|)
block|{
name|key
operator|+=
literal|"."
operator|+
name|pathElements
index|[
name|nextIndex
index|]
expr_stmt|;
name|currentValue
operator|=
name|part
operator|.
name|get
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|nextIndex
operator|++
expr_stmt|;
block|}
if|if
condition|(
name|currentValue
operator|==
literal|null
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|currentValue
operator|instanceof
name|Map
condition|)
block|{
name|extractRawValues
argument_list|(
name|values
argument_list|,
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|currentValue
argument_list|,
name|pathElements
argument_list|,
name|nextIndex
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|currentValue
operator|instanceof
name|List
condition|)
block|{
name|extractRawValues
argument_list|(
name|values
argument_list|,
operator|(
name|List
operator|)
name|currentValue
argument_list|,
name|pathElements
argument_list|,
name|nextIndex
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|values
operator|.
name|add
argument_list|(
name|currentValue
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"unchecked"
block|}
argument_list|)
DECL|method|extractRawValues
specifier|private
specifier|static
name|void
name|extractRawValues
parameter_list|(
name|List
name|values
parameter_list|,
name|List
argument_list|<
name|Object
argument_list|>
name|part
parameter_list|,
name|String
index|[]
name|pathElements
parameter_list|,
name|int
name|index
parameter_list|)
block|{
for|for
control|(
name|Object
name|value
range|:
name|part
control|)
block|{
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|value
operator|instanceof
name|Map
condition|)
block|{
name|extractRawValues
argument_list|(
name|values
argument_list|,
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
name|pathElements
argument_list|,
name|index
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
name|extractRawValues
argument_list|(
name|values
argument_list|,
operator|(
name|List
operator|)
name|value
argument_list|,
name|pathElements
argument_list|,
name|index
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|values
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|extractValue
specifier|public
specifier|static
name|Object
name|extractValue
parameter_list|(
name|String
name|path
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|map
parameter_list|)
block|{
name|String
index|[]
name|pathElements
init|=
name|path
operator|.
name|split
argument_list|(
literal|"\\."
argument_list|)
decl_stmt|;
if|if
condition|(
name|pathElements
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|extractValue
argument_list|(
name|pathElements
argument_list|,
literal|0
argument_list|,
name|map
argument_list|)
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"unchecked"
block|}
argument_list|)
DECL|method|extractValue
specifier|private
specifier|static
name|Object
name|extractValue
parameter_list|(
name|String
index|[]
name|pathElements
parameter_list|,
name|int
name|index
parameter_list|,
name|Object
name|currentValue
parameter_list|)
block|{
if|if
condition|(
name|index
operator|==
name|pathElements
operator|.
name|length
condition|)
block|{
return|return
name|currentValue
return|;
block|}
if|if
condition|(
name|currentValue
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|currentValue
operator|instanceof
name|Map
condition|)
block|{
name|Map
name|map
init|=
operator|(
name|Map
operator|)
name|currentValue
decl_stmt|;
name|String
name|key
init|=
name|pathElements
index|[
name|index
index|]
decl_stmt|;
name|Object
name|mapValue
init|=
name|map
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
name|int
name|nextIndex
init|=
name|index
operator|+
literal|1
decl_stmt|;
while|while
condition|(
name|mapValue
operator|==
literal|null
operator|&&
name|nextIndex
operator|!=
name|pathElements
operator|.
name|length
condition|)
block|{
name|key
operator|+=
literal|"."
operator|+
name|pathElements
index|[
name|nextIndex
index|]
expr_stmt|;
name|mapValue
operator|=
name|map
operator|.
name|get
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|nextIndex
operator|++
expr_stmt|;
block|}
return|return
name|extractValue
argument_list|(
name|pathElements
argument_list|,
name|nextIndex
argument_list|,
name|mapValue
argument_list|)
return|;
block|}
if|if
condition|(
name|currentValue
operator|instanceof
name|List
condition|)
block|{
name|List
name|valueList
init|=
operator|(
name|List
operator|)
name|currentValue
decl_stmt|;
name|List
name|newList
init|=
operator|new
name|ArrayList
argument_list|(
name|valueList
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Object
name|o
range|:
name|valueList
control|)
block|{
name|Object
name|listValue
init|=
name|extractValue
argument_list|(
name|pathElements
argument_list|,
name|index
argument_list|,
name|o
argument_list|)
decl_stmt|;
if|if
condition|(
name|listValue
operator|!=
literal|null
condition|)
block|{
name|newList
operator|.
name|add
argument_list|(
name|listValue
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|newList
return|;
block|}
return|return
literal|null
return|;
block|}
comment|/**      * Only keep properties in {@code map} that match the {@code includes} but      * not the {@code excludes}. An empty list of includes is interpreted as a      * wildcard while an empty list of excludes does not match anything.      *      * If a property matches both an include and an exclude, then the exclude      * wins.      *      * If an object matches, then any of its sub properties are automatically      * considered as matching as well, both for includes and excludes.      *      * Dots in field names are treated as sub objects. So for instance if a      * document contains {@code a.b} as a property and {@code a} is an include,      * then {@code a.b} will be kept in the filtered map.      */
DECL|method|filter
specifier|public
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|filter
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|?
argument_list|>
name|map
parameter_list|,
name|String
index|[]
name|includes
parameter_list|,
name|String
index|[]
name|excludes
parameter_list|)
block|{
return|return
name|filter
argument_list|(
name|includes
argument_list|,
name|excludes
argument_list|)
operator|.
name|apply
argument_list|(
name|map
argument_list|)
return|;
block|}
comment|/**      * Returns a function that filters a document map based on the given include and exclude rules.      * @see #filter(Map, String[], String[]) for details      */
DECL|method|filter
specifier|public
specifier|static
name|Function
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|?
argument_list|>
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|filter
parameter_list|(
name|String
index|[]
name|includes
parameter_list|,
name|String
index|[]
name|excludes
parameter_list|)
block|{
name|CharacterRunAutomaton
name|matchAllAutomaton
init|=
operator|new
name|CharacterRunAutomaton
argument_list|(
name|Automata
operator|.
name|makeAnyString
argument_list|()
argument_list|)
decl_stmt|;
name|CharacterRunAutomaton
name|include
decl_stmt|;
if|if
condition|(
name|includes
operator|==
literal|null
operator|||
name|includes
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|include
operator|=
name|matchAllAutomaton
expr_stmt|;
block|}
else|else
block|{
name|Automaton
name|includeA
init|=
name|Regex
operator|.
name|simpleMatchToAutomaton
argument_list|(
name|includes
argument_list|)
decl_stmt|;
name|includeA
operator|=
name|makeMatchDotsInFieldNames
argument_list|(
name|includeA
argument_list|)
expr_stmt|;
name|include
operator|=
operator|new
name|CharacterRunAutomaton
argument_list|(
name|includeA
argument_list|)
expr_stmt|;
block|}
name|Automaton
name|excludeA
decl_stmt|;
if|if
condition|(
name|excludes
operator|==
literal|null
operator|||
name|excludes
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|excludeA
operator|=
name|Automata
operator|.
name|makeEmpty
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|excludeA
operator|=
name|Regex
operator|.
name|simpleMatchToAutomaton
argument_list|(
name|excludes
argument_list|)
expr_stmt|;
name|excludeA
operator|=
name|makeMatchDotsInFieldNames
argument_list|(
name|excludeA
argument_list|)
expr_stmt|;
block|}
name|CharacterRunAutomaton
name|exclude
init|=
operator|new
name|CharacterRunAutomaton
argument_list|(
name|excludeA
argument_list|)
decl_stmt|;
comment|// NOTE: We cannot use Operations.minus because of the special case that
comment|// we want all sub properties to match as soon as an object matches
return|return
parameter_list|(
name|map
parameter_list|)
lambda|->
name|filter
argument_list|(
name|map
argument_list|,
name|include
argument_list|,
literal|0
argument_list|,
name|exclude
argument_list|,
literal|0
argument_list|,
name|matchAllAutomaton
argument_list|)
return|;
block|}
comment|/** Make matches on objects also match dots in field names.      *  For instance, if the original simple regex is `foo`, this will translate      *  it into `foo` OR `foo.*`. */
DECL|method|makeMatchDotsInFieldNames
specifier|private
specifier|static
name|Automaton
name|makeMatchDotsInFieldNames
parameter_list|(
name|Automaton
name|automaton
parameter_list|)
block|{
return|return
name|Operations
operator|.
name|union
argument_list|(
name|automaton
argument_list|,
name|Operations
operator|.
name|concatenate
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|automaton
argument_list|,
name|Automata
operator|.
name|makeChar
argument_list|(
literal|'.'
argument_list|)
argument_list|,
name|Automata
operator|.
name|makeAnyString
argument_list|()
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
DECL|method|step
specifier|private
specifier|static
name|int
name|step
parameter_list|(
name|CharacterRunAutomaton
name|automaton
parameter_list|,
name|String
name|key
parameter_list|,
name|int
name|state
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|state
operator|!=
operator|-
literal|1
operator|&&
name|i
operator|<
name|key
operator|.
name|length
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|state
operator|=
name|automaton
operator|.
name|step
argument_list|(
name|state
argument_list|,
name|key
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|state
return|;
block|}
DECL|method|filter
specifier|private
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|filter
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|?
argument_list|>
name|map
parameter_list|,
name|CharacterRunAutomaton
name|includeAutomaton
parameter_list|,
name|int
name|initialIncludeState
parameter_list|,
name|CharacterRunAutomaton
name|excludeAutomaton
parameter_list|,
name|int
name|initialExcludeState
parameter_list|,
name|CharacterRunAutomaton
name|matchAllAutomaton
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|filtered
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
name|?
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
decl_stmt|;
name|int
name|includeState
init|=
name|step
argument_list|(
name|includeAutomaton
argument_list|,
name|key
argument_list|,
name|initialIncludeState
argument_list|)
decl_stmt|;
if|if
condition|(
name|includeState
operator|==
operator|-
literal|1
condition|)
block|{
continue|continue;
block|}
name|int
name|excludeState
init|=
name|step
argument_list|(
name|excludeAutomaton
argument_list|,
name|key
argument_list|,
name|initialExcludeState
argument_list|)
decl_stmt|;
if|if
condition|(
name|excludeState
operator|!=
operator|-
literal|1
operator|&&
name|excludeAutomaton
operator|.
name|isAccept
argument_list|(
name|excludeState
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|Object
name|value
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|CharacterRunAutomaton
name|subIncludeAutomaton
init|=
name|includeAutomaton
decl_stmt|;
name|int
name|subIncludeState
init|=
name|includeState
decl_stmt|;
if|if
condition|(
name|includeAutomaton
operator|.
name|isAccept
argument_list|(
name|includeState
argument_list|)
condition|)
block|{
if|if
condition|(
name|excludeState
operator|==
operator|-
literal|1
operator|||
name|excludeAutomaton
operator|.
name|step
argument_list|(
name|excludeState
argument_list|,
literal|'.'
argument_list|)
operator|==
operator|-
literal|1
condition|)
block|{
comment|// the exclude has no chances to match inner properties
name|filtered
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
continue|continue;
block|}
else|else
block|{
comment|// the object matched, so consider that the include matches every inner property
comment|// we only care about excludes now
name|subIncludeAutomaton
operator|=
name|matchAllAutomaton
expr_stmt|;
name|subIncludeState
operator|=
literal|0
expr_stmt|;
block|}
block|}
if|if
condition|(
name|value
operator|instanceof
name|Map
condition|)
block|{
name|subIncludeState
operator|=
name|subIncludeAutomaton
operator|.
name|step
argument_list|(
name|subIncludeState
argument_list|,
literal|'.'
argument_list|)
expr_stmt|;
if|if
condition|(
name|subIncludeState
operator|==
operator|-
literal|1
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|excludeState
operator|!=
operator|-
literal|1
condition|)
block|{
name|excludeState
operator|=
name|excludeAutomaton
operator|.
name|step
argument_list|(
name|excludeState
argument_list|,
literal|'.'
argument_list|)
expr_stmt|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|valueAsMap
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|value
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|filteredValue
init|=
name|filter
argument_list|(
name|valueAsMap
argument_list|,
name|subIncludeAutomaton
argument_list|,
name|subIncludeState
argument_list|,
name|excludeAutomaton
argument_list|,
name|excludeState
argument_list|,
name|matchAllAutomaton
argument_list|)
decl_stmt|;
if|if
condition|(
name|includeAutomaton
operator|.
name|isAccept
argument_list|(
name|includeState
argument_list|)
operator|||
name|filteredValue
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
condition|)
block|{
name|filtered
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|filteredValue
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|value
operator|instanceof
name|Iterable
condition|)
block|{
name|List
argument_list|<
name|Object
argument_list|>
name|filteredValue
init|=
name|filter
argument_list|(
operator|(
name|Iterable
argument_list|<
name|?
argument_list|>
operator|)
name|value
argument_list|,
name|subIncludeAutomaton
argument_list|,
name|subIncludeState
argument_list|,
name|excludeAutomaton
argument_list|,
name|excludeState
argument_list|,
name|matchAllAutomaton
argument_list|)
decl_stmt|;
if|if
condition|(
name|includeAutomaton
operator|.
name|isAccept
argument_list|(
name|includeState
argument_list|)
operator|||
name|filteredValue
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
condition|)
block|{
name|filtered
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|filteredValue
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// leaf property
if|if
condition|(
name|includeAutomaton
operator|.
name|isAccept
argument_list|(
name|includeState
argument_list|)
operator|&&
operator|(
name|excludeState
operator|==
operator|-
literal|1
operator|||
name|excludeAutomaton
operator|.
name|isAccept
argument_list|(
name|excludeState
argument_list|)
operator|==
literal|false
operator|)
condition|)
block|{
name|filtered
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|filtered
return|;
block|}
DECL|method|filter
specifier|private
specifier|static
name|List
argument_list|<
name|Object
argument_list|>
name|filter
parameter_list|(
name|Iterable
argument_list|<
name|?
argument_list|>
name|iterable
parameter_list|,
name|CharacterRunAutomaton
name|includeAutomaton
parameter_list|,
name|int
name|initialIncludeState
parameter_list|,
name|CharacterRunAutomaton
name|excludeAutomaton
parameter_list|,
name|int
name|initialExcludeState
parameter_list|,
name|CharacterRunAutomaton
name|matchAllAutomaton
parameter_list|)
block|{
name|List
argument_list|<
name|Object
argument_list|>
name|filtered
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Object
name|value
range|:
name|iterable
control|)
block|{
if|if
condition|(
name|value
operator|instanceof
name|Map
condition|)
block|{
name|int
name|includeState
init|=
name|includeAutomaton
operator|.
name|step
argument_list|(
name|initialIncludeState
argument_list|,
literal|'.'
argument_list|)
decl_stmt|;
name|int
name|excludeState
init|=
name|initialExcludeState
decl_stmt|;
if|if
condition|(
name|excludeState
operator|!=
operator|-
literal|1
condition|)
block|{
name|excludeState
operator|=
name|excludeAutomaton
operator|.
name|step
argument_list|(
name|excludeState
argument_list|,
literal|'.'
argument_list|)
expr_stmt|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|filteredValue
init|=
name|filter
argument_list|(
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|?
argument_list|>
operator|)
name|value
argument_list|,
name|includeAutomaton
argument_list|,
name|includeState
argument_list|,
name|excludeAutomaton
argument_list|,
name|excludeState
argument_list|,
name|matchAllAutomaton
argument_list|)
decl_stmt|;
if|if
condition|(
name|filteredValue
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
condition|)
block|{
name|filtered
operator|.
name|add
argument_list|(
name|filteredValue
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|value
operator|instanceof
name|Iterable
condition|)
block|{
name|List
argument_list|<
name|Object
argument_list|>
name|filteredValue
init|=
name|filter
argument_list|(
operator|(
name|Iterable
argument_list|<
name|?
argument_list|>
operator|)
name|value
argument_list|,
name|includeAutomaton
argument_list|,
name|initialIncludeState
argument_list|,
name|excludeAutomaton
argument_list|,
name|initialExcludeState
argument_list|,
name|matchAllAutomaton
argument_list|)
decl_stmt|;
if|if
condition|(
name|filteredValue
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
condition|)
block|{
name|filtered
operator|.
name|add
argument_list|(
name|filteredValue
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// TODO: we have tests relying on this behavior on arrays even
comment|// if the path does not match, but this looks like a bug?
name|filtered
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|filtered
return|;
block|}
DECL|method|isObject
specifier|public
specifier|static
name|boolean
name|isObject
parameter_list|(
name|Object
name|node
parameter_list|)
block|{
return|return
name|node
operator|instanceof
name|Map
return|;
block|}
DECL|method|isArray
specifier|public
specifier|static
name|boolean
name|isArray
parameter_list|(
name|Object
name|node
parameter_list|)
block|{
return|return
name|node
operator|instanceof
name|List
return|;
block|}
DECL|method|nodeStringValue
specifier|public
specifier|static
name|String
name|nodeStringValue
parameter_list|(
name|Object
name|node
parameter_list|,
name|String
name|defaultValue
parameter_list|)
block|{
if|if
condition|(
name|node
operator|==
literal|null
condition|)
block|{
return|return
name|defaultValue
return|;
block|}
return|return
name|node
operator|.
name|toString
argument_list|()
return|;
block|}
DECL|method|nodeFloatValue
specifier|public
specifier|static
name|float
name|nodeFloatValue
parameter_list|(
name|Object
name|node
parameter_list|,
name|float
name|defaultValue
parameter_list|)
block|{
if|if
condition|(
name|node
operator|==
literal|null
condition|)
block|{
return|return
name|defaultValue
return|;
block|}
return|return
name|nodeFloatValue
argument_list|(
name|node
argument_list|)
return|;
block|}
DECL|method|nodeFloatValue
specifier|public
specifier|static
name|float
name|nodeFloatValue
parameter_list|(
name|Object
name|node
parameter_list|)
block|{
if|if
condition|(
name|node
operator|instanceof
name|Number
condition|)
block|{
return|return
operator|(
operator|(
name|Number
operator|)
name|node
operator|)
operator|.
name|floatValue
argument_list|()
return|;
block|}
return|return
name|Float
operator|.
name|parseFloat
argument_list|(
name|node
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
DECL|method|nodeDoubleValue
specifier|public
specifier|static
name|double
name|nodeDoubleValue
parameter_list|(
name|Object
name|node
parameter_list|,
name|double
name|defaultValue
parameter_list|)
block|{
if|if
condition|(
name|node
operator|==
literal|null
condition|)
block|{
return|return
name|defaultValue
return|;
block|}
return|return
name|nodeDoubleValue
argument_list|(
name|node
argument_list|)
return|;
block|}
DECL|method|nodeDoubleValue
specifier|public
specifier|static
name|double
name|nodeDoubleValue
parameter_list|(
name|Object
name|node
parameter_list|)
block|{
if|if
condition|(
name|node
operator|instanceof
name|Number
condition|)
block|{
return|return
operator|(
operator|(
name|Number
operator|)
name|node
operator|)
operator|.
name|doubleValue
argument_list|()
return|;
block|}
return|return
name|Double
operator|.
name|parseDouble
argument_list|(
name|node
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
DECL|method|nodeIntegerValue
specifier|public
specifier|static
name|int
name|nodeIntegerValue
parameter_list|(
name|Object
name|node
parameter_list|)
block|{
if|if
condition|(
name|node
operator|instanceof
name|Number
condition|)
block|{
return|return
name|Numbers
operator|.
name|toIntExact
argument_list|(
operator|(
name|Number
operator|)
name|node
argument_list|)
return|;
block|}
return|return
name|Integer
operator|.
name|parseInt
argument_list|(
name|node
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
DECL|method|nodeIntegerValue
specifier|public
specifier|static
name|int
name|nodeIntegerValue
parameter_list|(
name|Object
name|node
parameter_list|,
name|int
name|defaultValue
parameter_list|)
block|{
if|if
condition|(
name|node
operator|==
literal|null
condition|)
block|{
return|return
name|defaultValue
return|;
block|}
return|return
name|nodeIntegerValue
argument_list|(
name|node
argument_list|)
return|;
block|}
DECL|method|nodeShortValue
specifier|public
specifier|static
name|short
name|nodeShortValue
parameter_list|(
name|Object
name|node
parameter_list|,
name|short
name|defaultValue
parameter_list|)
block|{
if|if
condition|(
name|node
operator|==
literal|null
condition|)
block|{
return|return
name|defaultValue
return|;
block|}
return|return
name|nodeShortValue
argument_list|(
name|node
argument_list|)
return|;
block|}
DECL|method|nodeShortValue
specifier|public
specifier|static
name|short
name|nodeShortValue
parameter_list|(
name|Object
name|node
parameter_list|)
block|{
if|if
condition|(
name|node
operator|instanceof
name|Number
condition|)
block|{
return|return
name|Numbers
operator|.
name|toShortExact
argument_list|(
operator|(
name|Number
operator|)
name|node
argument_list|)
return|;
block|}
return|return
name|Short
operator|.
name|parseShort
argument_list|(
name|node
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
DECL|method|nodeByteValue
specifier|public
specifier|static
name|byte
name|nodeByteValue
parameter_list|(
name|Object
name|node
parameter_list|,
name|byte
name|defaultValue
parameter_list|)
block|{
if|if
condition|(
name|node
operator|==
literal|null
condition|)
block|{
return|return
name|defaultValue
return|;
block|}
return|return
name|nodeByteValue
argument_list|(
name|node
argument_list|)
return|;
block|}
DECL|method|nodeByteValue
specifier|public
specifier|static
name|byte
name|nodeByteValue
parameter_list|(
name|Object
name|node
parameter_list|)
block|{
if|if
condition|(
name|node
operator|instanceof
name|Number
condition|)
block|{
return|return
name|Numbers
operator|.
name|toByteExact
argument_list|(
operator|(
name|Number
operator|)
name|node
argument_list|)
return|;
block|}
return|return
name|Byte
operator|.
name|parseByte
argument_list|(
name|node
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
DECL|method|nodeLongValue
specifier|public
specifier|static
name|long
name|nodeLongValue
parameter_list|(
name|Object
name|node
parameter_list|,
name|long
name|defaultValue
parameter_list|)
block|{
if|if
condition|(
name|node
operator|==
literal|null
condition|)
block|{
return|return
name|defaultValue
return|;
block|}
return|return
name|nodeLongValue
argument_list|(
name|node
argument_list|)
return|;
block|}
DECL|method|nodeLongValue
specifier|public
specifier|static
name|long
name|nodeLongValue
parameter_list|(
name|Object
name|node
parameter_list|)
block|{
if|if
condition|(
name|node
operator|instanceof
name|Number
condition|)
block|{
return|return
name|Numbers
operator|.
name|toLongExact
argument_list|(
operator|(
name|Number
operator|)
name|node
argument_list|)
return|;
block|}
return|return
name|Long
operator|.
name|parseLong
argument_list|(
name|node
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
comment|/**      * This method is very lenient, use {@link #nodeBooleanValue} instead.      */
DECL|method|lenientNodeBooleanValue
specifier|public
specifier|static
name|boolean
name|lenientNodeBooleanValue
parameter_list|(
name|Object
name|node
parameter_list|,
name|boolean
name|defaultValue
parameter_list|)
block|{
if|if
condition|(
name|node
operator|==
literal|null
condition|)
block|{
return|return
name|defaultValue
return|;
block|}
return|return
name|lenientNodeBooleanValue
argument_list|(
name|node
argument_list|)
return|;
block|}
comment|/**      * This method is very lenient, use {@link #nodeBooleanValue} instead.      */
DECL|method|lenientNodeBooleanValue
specifier|public
specifier|static
name|boolean
name|lenientNodeBooleanValue
parameter_list|(
name|Object
name|node
parameter_list|)
block|{
if|if
condition|(
name|node
operator|instanceof
name|Boolean
condition|)
block|{
return|return
operator|(
name|Boolean
operator|)
name|node
return|;
block|}
if|if
condition|(
name|node
operator|instanceof
name|Number
condition|)
block|{
return|return
operator|(
operator|(
name|Number
operator|)
name|node
operator|)
operator|.
name|intValue
argument_list|()
operator|!=
literal|0
return|;
block|}
name|String
name|value
init|=
name|node
operator|.
name|toString
argument_list|()
decl_stmt|;
return|return
operator|!
operator|(
name|value
operator|.
name|equals
argument_list|(
literal|"false"
argument_list|)
operator|||
name|value
operator|.
name|equals
argument_list|(
literal|"0"
argument_list|)
operator|||
name|value
operator|.
name|equals
argument_list|(
literal|"off"
argument_list|)
operator|)
return|;
block|}
DECL|method|nodeBooleanValue
specifier|public
specifier|static
name|boolean
name|nodeBooleanValue
parameter_list|(
name|Object
name|node
parameter_list|)
block|{
switch|switch
condition|(
name|node
operator|.
name|toString
argument_list|()
condition|)
block|{
case|case
literal|"true"
case|:
return|return
literal|true
return|;
case|case
literal|"false"
case|:
return|return
literal|false
return|;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Can't parse boolean value ["
operator|+
name|node
operator|+
literal|"], expected [true] or [false]"
argument_list|)
throw|;
block|}
block|}
DECL|method|nodeTimeValue
specifier|public
specifier|static
name|TimeValue
name|nodeTimeValue
parameter_list|(
name|Object
name|node
parameter_list|,
name|TimeValue
name|defaultValue
parameter_list|)
block|{
if|if
condition|(
name|node
operator|==
literal|null
condition|)
block|{
return|return
name|defaultValue
return|;
block|}
return|return
name|nodeTimeValue
argument_list|(
name|node
argument_list|)
return|;
block|}
DECL|method|nodeTimeValue
specifier|public
specifier|static
name|TimeValue
name|nodeTimeValue
parameter_list|(
name|Object
name|node
parameter_list|)
block|{
if|if
condition|(
name|node
operator|instanceof
name|Number
condition|)
block|{
return|return
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
operator|(
operator|(
name|Number
operator|)
name|node
operator|)
operator|.
name|longValue
argument_list|()
argument_list|)
return|;
block|}
return|return
name|TimeValue
operator|.
name|parseTimeValue
argument_list|(
name|node
operator|.
name|toString
argument_list|()
argument_list|,
literal|null
argument_list|,
name|XContentMapValues
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|".nodeTimeValue"
argument_list|)
return|;
block|}
DECL|method|nodeMapValue
specifier|public
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|nodeMapValue
parameter_list|(
name|Object
name|node
parameter_list|,
name|String
name|desc
parameter_list|)
block|{
if|if
condition|(
name|node
operator|instanceof
name|Map
condition|)
block|{
return|return
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|node
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
name|desc
operator|+
literal|" should be a hash but was of type: "
operator|+
name|node
operator|.
name|getClass
argument_list|()
argument_list|)
throw|;
block|}
block|}
comment|/**      * Returns an array of string value from a node value.      *      * If the node represents an array the corresponding array of strings is returned.      * Otherwise the node is treated as a comma-separated string.      */
DECL|method|nodeStringArrayValue
specifier|public
specifier|static
name|String
index|[]
name|nodeStringArrayValue
parameter_list|(
name|Object
name|node
parameter_list|)
block|{
if|if
condition|(
name|isArray
argument_list|(
name|node
argument_list|)
condition|)
block|{
name|List
name|list
init|=
operator|(
name|List
operator|)
name|node
decl_stmt|;
name|String
index|[]
name|arr
init|=
operator|new
name|String
index|[
name|list
operator|.
name|size
argument_list|()
index|]
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
name|arr
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|arr
index|[
name|i
index|]
operator|=
name|nodeStringValue
argument_list|(
name|list
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
return|return
name|arr
return|;
block|}
else|else
block|{
return|return
name|Strings
operator|.
name|splitStringByCommaToArray
argument_list|(
name|node
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

