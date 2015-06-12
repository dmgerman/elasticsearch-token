begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_comment
comment|/**  * A class to match character code points.  */
end_comment

begin_interface
DECL|interface|CharMatcher
specifier|public
interface|interface
name|CharMatcher
block|{
DECL|class|ByUnicodeCategory
specifier|public
specifier|static
class|class
name|ByUnicodeCategory
implements|implements
name|CharMatcher
block|{
DECL|method|of
specifier|public
specifier|static
name|CharMatcher
name|of
parameter_list|(
name|byte
name|unicodeCategory
parameter_list|)
block|{
return|return
operator|new
name|ByUnicodeCategory
argument_list|(
name|unicodeCategory
argument_list|)
return|;
block|}
DECL|field|unicodeType
specifier|private
specifier|final
name|byte
name|unicodeType
decl_stmt|;
DECL|method|ByUnicodeCategory
name|ByUnicodeCategory
parameter_list|(
name|byte
name|unicodeType
parameter_list|)
block|{
name|this
operator|.
name|unicodeType
operator|=
name|unicodeType
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|isTokenChar
specifier|public
name|boolean
name|isTokenChar
parameter_list|(
name|int
name|c
parameter_list|)
block|{
return|return
name|Character
operator|.
name|getType
argument_list|(
name|c
argument_list|)
operator|==
name|unicodeType
return|;
block|}
block|}
DECL|enum|Basic
specifier|public
enum|enum
name|Basic
implements|implements
name|CharMatcher
block|{
DECL|enum constant|LETTER
name|LETTER
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|isTokenChar
parameter_list|(
name|int
name|c
parameter_list|)
block|{
return|return
name|Character
operator|.
name|isLetter
argument_list|(
name|c
argument_list|)
return|;
block|}
block|}
block|,
DECL|enum constant|DIGIT
name|DIGIT
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|isTokenChar
parameter_list|(
name|int
name|c
parameter_list|)
block|{
return|return
name|Character
operator|.
name|isDigit
argument_list|(
name|c
argument_list|)
return|;
block|}
block|}
block|,
DECL|enum constant|WHITESPACE
name|WHITESPACE
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|isTokenChar
parameter_list|(
name|int
name|c
parameter_list|)
block|{
return|return
name|Character
operator|.
name|isWhitespace
argument_list|(
name|c
argument_list|)
return|;
block|}
block|}
block|,
DECL|enum constant|PUNCTUATION
name|PUNCTUATION
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|isTokenChar
parameter_list|(
name|int
name|c
parameter_list|)
block|{
switch|switch
condition|(
name|Character
operator|.
name|getType
argument_list|(
name|c
argument_list|)
condition|)
block|{
case|case
name|Character
operator|.
name|START_PUNCTUATION
case|:
case|case
name|Character
operator|.
name|END_PUNCTUATION
case|:
case|case
name|Character
operator|.
name|OTHER_PUNCTUATION
case|:
case|case
name|Character
operator|.
name|CONNECTOR_PUNCTUATION
case|:
case|case
name|Character
operator|.
name|DASH_PUNCTUATION
case|:
case|case
name|Character
operator|.
name|INITIAL_QUOTE_PUNCTUATION
case|:
case|case
name|Character
operator|.
name|FINAL_QUOTE_PUNCTUATION
case|:
return|return
literal|true
return|;
default|default:
return|return
literal|false
return|;
block|}
block|}
block|}
block|,
DECL|enum constant|SYMBOL
name|SYMBOL
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|isTokenChar
parameter_list|(
name|int
name|c
parameter_list|)
block|{
switch|switch
condition|(
name|Character
operator|.
name|getType
argument_list|(
name|c
argument_list|)
condition|)
block|{
case|case
name|Character
operator|.
name|CURRENCY_SYMBOL
case|:
case|case
name|Character
operator|.
name|MATH_SYMBOL
case|:
case|case
name|Character
operator|.
name|OTHER_SYMBOL
case|:
return|return
literal|true
return|;
default|default:
return|return
literal|false
return|;
block|}
block|}
block|}
block|}
DECL|class|Builder
specifier|public
specifier|final
class|class
name|Builder
block|{
DECL|field|matchers
specifier|private
specifier|final
name|Set
argument_list|<
name|CharMatcher
argument_list|>
name|matchers
decl_stmt|;
DECL|method|Builder
name|Builder
parameter_list|()
block|{
name|matchers
operator|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
expr_stmt|;
block|}
DECL|method|or
specifier|public
name|Builder
name|or
parameter_list|(
name|CharMatcher
name|matcher
parameter_list|)
block|{
name|matchers
operator|.
name|add
argument_list|(
name|matcher
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|build
specifier|public
name|CharMatcher
name|build
parameter_list|()
block|{
switch|switch
condition|(
name|matchers
operator|.
name|size
argument_list|()
condition|)
block|{
case|case
literal|0
case|:
return|return
operator|new
name|CharMatcher
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|isTokenChar
parameter_list|(
name|int
name|c
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|;
case|case
literal|1
case|:
return|return
name|matchers
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
return|;
default|default:
return|return
operator|new
name|CharMatcher
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|isTokenChar
parameter_list|(
name|int
name|c
parameter_list|)
block|{
for|for
control|(
name|CharMatcher
name|matcher
range|:
name|matchers
control|)
block|{
if|if
condition|(
name|matcher
operator|.
name|isTokenChar
argument_list|(
name|c
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
block|}
return|;
block|}
block|}
block|}
comment|/** Returns true if, and only if, the provided character matches this character class. */
DECL|method|isTokenChar
specifier|public
name|boolean
name|isTokenChar
parameter_list|(
name|int
name|c
parameter_list|)
function_decl|;
block|}
end_interface

end_unit
