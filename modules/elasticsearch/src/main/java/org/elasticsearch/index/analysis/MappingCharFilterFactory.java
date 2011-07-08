begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.analysis
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|analysis
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
name|analysis
operator|.
name|CharStream
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
name|analysis
operator|.
name|MappingCharFilter
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
name|analysis
operator|.
name|NormalizeCharMap
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchIllegalArgumentException
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
name|inject
operator|.
name|Inject
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
name|inject
operator|.
name|assistedinject
operator|.
name|Assisted
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
name|settings
operator|.
name|Settings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|env
operator|.
name|Environment
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
name|Index
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
name|settings
operator|.
name|IndexSettings
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

begin_class
annotation|@
name|AnalysisSettingsRequired
DECL|class|MappingCharFilterFactory
specifier|public
class|class
name|MappingCharFilterFactory
extends|extends
name|AbstractCharFilterFactory
block|{
DECL|field|normMap
specifier|private
specifier|final
name|NormalizeCharMap
name|normMap
decl_stmt|;
DECL|method|MappingCharFilterFactory
annotation|@
name|Inject
specifier|public
name|MappingCharFilterFactory
parameter_list|(
name|Index
name|index
parameter_list|,
annotation|@
name|IndexSettings
name|Settings
name|indexSettings
parameter_list|,
name|Environment
name|env
parameter_list|,
annotation|@
name|Assisted
name|String
name|name
parameter_list|,
annotation|@
name|Assisted
name|Settings
name|settings
parameter_list|)
block|{
name|super
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|,
name|name
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|rules
init|=
name|Analysis
operator|.
name|getWordList
argument_list|(
name|env
argument_list|,
name|settings
argument_list|,
literal|"mappings"
argument_list|)
decl_stmt|;
if|if
condition|(
name|rules
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"mapping requires either `mappings` or `mappings_path` to be configured"
argument_list|)
throw|;
block|}
name|normMap
operator|=
operator|new
name|NormalizeCharMap
argument_list|()
expr_stmt|;
name|parseRules
argument_list|(
name|rules
argument_list|,
name|normMap
argument_list|)
expr_stmt|;
block|}
DECL|method|create
annotation|@
name|Override
specifier|public
name|CharStream
name|create
parameter_list|(
name|CharStream
name|tokenStream
parameter_list|)
block|{
return|return
operator|new
name|MappingCharFilter
argument_list|(
name|normMap
argument_list|,
name|tokenStream
argument_list|)
return|;
block|}
comment|// source => target
DECL|field|rulePattern
specifier|private
specifier|static
name|Pattern
name|rulePattern
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"(.*)\\s*=>\\s*(.*)\\s*$"
argument_list|)
decl_stmt|;
comment|/**      * parses a list of MappingCharFilter style rules into a normalize char map      */
DECL|method|parseRules
specifier|private
name|void
name|parseRules
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|rules
parameter_list|,
name|NormalizeCharMap
name|map
parameter_list|)
block|{
for|for
control|(
name|String
name|rule
range|:
name|rules
control|)
block|{
name|Matcher
name|m
init|=
name|rulePattern
operator|.
name|matcher
argument_list|(
name|rule
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|m
operator|.
name|find
argument_list|()
condition|)
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Invalid Mapping Rule : ["
operator|+
name|rule
operator|+
literal|"]"
argument_list|)
throw|;
name|String
name|lhs
init|=
name|parseString
argument_list|(
name|m
operator|.
name|group
argument_list|(
literal|1
argument_list|)
operator|.
name|trim
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|rhs
init|=
name|parseString
argument_list|(
name|m
operator|.
name|group
argument_list|(
literal|2
argument_list|)
operator|.
name|trim
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|lhs
operator|==
literal|null
operator|||
name|rhs
operator|==
literal|null
condition|)
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Invalid Mapping Rule : ["
operator|+
name|rule
operator|+
literal|"]. Illegal mapping."
argument_list|)
throw|;
name|map
operator|.
name|add
argument_list|(
name|lhs
argument_list|,
name|rhs
argument_list|)
expr_stmt|;
block|}
block|}
DECL|field|out
name|char
index|[]
name|out
init|=
operator|new
name|char
index|[
literal|256
index|]
decl_stmt|;
DECL|method|parseString
specifier|private
name|String
name|parseString
parameter_list|(
name|String
name|s
parameter_list|)
block|{
name|int
name|readPos
init|=
literal|0
decl_stmt|;
name|int
name|len
init|=
name|s
operator|.
name|length
argument_list|()
decl_stmt|;
name|int
name|writePos
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|readPos
operator|<
name|len
condition|)
block|{
name|char
name|c
init|=
name|s
operator|.
name|charAt
argument_list|(
name|readPos
operator|++
argument_list|)
decl_stmt|;
if|if
condition|(
name|c
operator|==
literal|'\\'
condition|)
block|{
if|if
condition|(
name|readPos
operator|>=
name|len
condition|)
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Invalid escaped char in ["
operator|+
name|s
operator|+
literal|"]"
argument_list|)
throw|;
name|c
operator|=
name|s
operator|.
name|charAt
argument_list|(
name|readPos
operator|++
argument_list|)
expr_stmt|;
switch|switch
condition|(
name|c
condition|)
block|{
case|case
literal|'\\'
case|:
name|c
operator|=
literal|'\\'
expr_stmt|;
break|break;
case|case
literal|'n'
case|:
name|c
operator|=
literal|'\n'
expr_stmt|;
break|break;
case|case
literal|'t'
case|:
name|c
operator|=
literal|'\t'
expr_stmt|;
break|break;
case|case
literal|'r'
case|:
name|c
operator|=
literal|'\r'
expr_stmt|;
break|break;
case|case
literal|'b'
case|:
name|c
operator|=
literal|'\b'
expr_stmt|;
break|break;
case|case
literal|'f'
case|:
name|c
operator|=
literal|'\f'
expr_stmt|;
break|break;
case|case
literal|'u'
case|:
if|if
condition|(
name|readPos
operator|+
literal|3
operator|>=
name|len
condition|)
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Invalid escaped char in ["
operator|+
name|s
operator|+
literal|"]"
argument_list|)
throw|;
name|c
operator|=
operator|(
name|char
operator|)
name|Integer
operator|.
name|parseInt
argument_list|(
name|s
operator|.
name|substring
argument_list|(
name|readPos
argument_list|,
name|readPos
operator|+
literal|4
argument_list|)
argument_list|,
literal|16
argument_list|)
expr_stmt|;
name|readPos
operator|+=
literal|4
expr_stmt|;
break|break;
block|}
block|}
name|out
index|[
name|writePos
operator|++
index|]
operator|=
name|c
expr_stmt|;
block|}
return|return
operator|new
name|String
argument_list|(
name|out
argument_list|,
literal|0
argument_list|,
name|writePos
argument_list|)
return|;
block|}
block|}
end_class

end_unit

