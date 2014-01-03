begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.regex
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|regex
package|;
end_package

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
name|Strings
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Locale
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|Regex
specifier|public
class|class
name|Regex
block|{
comment|/**      * This Regex / {@link Pattern} flag is supported from Java 7 on.      * If set on a Java6 JVM the flag will be ignored.      */
DECL|field|UNICODE_CHARACTER_CLASS
specifier|public
specifier|static
specifier|final
name|int
name|UNICODE_CHARACTER_CLASS
init|=
literal|0x100
decl_stmt|;
comment|// supported in JAVA7
comment|/**      * Is the str a simple match pattern.      */
DECL|method|isSimpleMatchPattern
specifier|public
specifier|static
name|boolean
name|isSimpleMatchPattern
parameter_list|(
name|String
name|str
parameter_list|)
block|{
return|return
name|str
operator|.
name|indexOf
argument_list|(
literal|'*'
argument_list|)
operator|!=
operator|-
literal|1
return|;
block|}
DECL|method|isMatchAllPattern
specifier|public
specifier|static
name|boolean
name|isMatchAllPattern
parameter_list|(
name|String
name|str
parameter_list|)
block|{
return|return
name|str
operator|.
name|equals
argument_list|(
literal|"*"
argument_list|)
return|;
block|}
comment|/**      * Match a String against the given pattern, supporting the following simple      * pattern styles: "xxx*", "*xxx", "*xxx*" and "xxx*yyy" matches (with an      * arbitrary number of pattern parts), as well as direct equality.      *      * @param pattern the pattern to match against      * @param str     the String to match      * @return whether the String matches the given pattern      */
DECL|method|simpleMatch
specifier|public
specifier|static
name|boolean
name|simpleMatch
parameter_list|(
name|String
name|pattern
parameter_list|,
name|String
name|str
parameter_list|)
block|{
if|if
condition|(
name|pattern
operator|==
literal|null
operator|||
name|str
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
name|int
name|firstIndex
init|=
name|pattern
operator|.
name|indexOf
argument_list|(
literal|'*'
argument_list|)
decl_stmt|;
if|if
condition|(
name|firstIndex
operator|==
operator|-
literal|1
condition|)
block|{
return|return
name|pattern
operator|.
name|equals
argument_list|(
name|str
argument_list|)
return|;
block|}
if|if
condition|(
name|firstIndex
operator|==
literal|0
condition|)
block|{
if|if
condition|(
name|pattern
operator|.
name|length
argument_list|()
operator|==
literal|1
condition|)
block|{
return|return
literal|true
return|;
block|}
name|int
name|nextIndex
init|=
name|pattern
operator|.
name|indexOf
argument_list|(
literal|'*'
argument_list|,
name|firstIndex
operator|+
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|nextIndex
operator|==
operator|-
literal|1
condition|)
block|{
return|return
name|str
operator|.
name|endsWith
argument_list|(
name|pattern
operator|.
name|substring
argument_list|(
literal|1
argument_list|)
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|nextIndex
operator|==
literal|1
condition|)
block|{
comment|// Double wildcard "**" - skipping the first "*"
return|return
name|simpleMatch
argument_list|(
name|pattern
operator|.
name|substring
argument_list|(
literal|1
argument_list|)
argument_list|,
name|str
argument_list|)
return|;
block|}
name|String
name|part
init|=
name|pattern
operator|.
name|substring
argument_list|(
literal|1
argument_list|,
name|nextIndex
argument_list|)
decl_stmt|;
name|int
name|partIndex
init|=
name|str
operator|.
name|indexOf
argument_list|(
name|part
argument_list|)
decl_stmt|;
while|while
condition|(
name|partIndex
operator|!=
operator|-
literal|1
condition|)
block|{
if|if
condition|(
name|simpleMatch
argument_list|(
name|pattern
operator|.
name|substring
argument_list|(
name|nextIndex
argument_list|)
argument_list|,
name|str
operator|.
name|substring
argument_list|(
name|partIndex
operator|+
name|part
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
name|partIndex
operator|=
name|str
operator|.
name|indexOf
argument_list|(
name|part
argument_list|,
name|partIndex
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
return|return
literal|false
return|;
block|}
return|return
operator|(
name|str
operator|.
name|length
argument_list|()
operator|>=
name|firstIndex
operator|&&
name|pattern
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|firstIndex
argument_list|)
operator|.
name|equals
argument_list|(
name|str
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|firstIndex
argument_list|)
argument_list|)
operator|&&
name|simpleMatch
argument_list|(
name|pattern
operator|.
name|substring
argument_list|(
name|firstIndex
argument_list|)
argument_list|,
name|str
operator|.
name|substring
argument_list|(
name|firstIndex
argument_list|)
argument_list|)
operator|)
return|;
block|}
comment|/**      * Match a String against the given patterns, supporting the following simple      * pattern styles: "xxx*", "*xxx", "*xxx*" and "xxx*yyy" matches (with an      * arbitrary number of pattern parts), as well as direct equality.      *      * @param patterns the patterns to match against      * @param str      the String to match      * @return whether the String matches any of the given patterns      */
DECL|method|simpleMatch
specifier|public
specifier|static
name|boolean
name|simpleMatch
parameter_list|(
name|String
index|[]
name|patterns
parameter_list|,
name|String
name|str
parameter_list|)
block|{
if|if
condition|(
name|patterns
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
name|pattern
range|:
name|patterns
control|)
block|{
if|if
condition|(
name|simpleMatch
argument_list|(
name|pattern
argument_list|,
name|str
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
block|}
return|return
literal|false
return|;
block|}
DECL|method|simpleMatch
specifier|public
specifier|static
name|boolean
name|simpleMatch
parameter_list|(
name|String
index|[]
name|patterns
parameter_list|,
name|String
index|[]
name|types
parameter_list|)
block|{
if|if
condition|(
name|patterns
operator|!=
literal|null
operator|&&
name|types
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
name|type
range|:
name|types
control|)
block|{
for|for
control|(
name|String
name|pattern
range|:
name|patterns
control|)
block|{
if|if
condition|(
name|simpleMatch
argument_list|(
name|pattern
argument_list|,
name|type
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
block|}
block|}
return|return
literal|false
return|;
block|}
DECL|method|compile
specifier|public
specifier|static
name|Pattern
name|compile
parameter_list|(
name|String
name|regex
parameter_list|,
name|String
name|flags
parameter_list|)
block|{
name|int
name|pFlags
init|=
name|flags
operator|==
literal|null
condition|?
literal|0
else|:
name|flagsFromString
argument_list|(
name|flags
argument_list|)
decl_stmt|;
return|return
name|Pattern
operator|.
name|compile
argument_list|(
name|regex
argument_list|,
name|pFlags
argument_list|)
return|;
block|}
DECL|method|flagsFromString
specifier|public
specifier|static
name|int
name|flagsFromString
parameter_list|(
name|String
name|flags
parameter_list|)
block|{
name|int
name|pFlags
init|=
literal|0
decl_stmt|;
for|for
control|(
name|String
name|s
range|:
name|Strings
operator|.
name|delimitedListToStringArray
argument_list|(
name|flags
argument_list|,
literal|"|"
argument_list|)
control|)
block|{
if|if
condition|(
name|s
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
continue|continue;
block|}
name|s
operator|=
name|s
operator|.
name|toUpperCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
expr_stmt|;
if|if
condition|(
literal|"CASE_INSENSITIVE"
operator|.
name|equals
argument_list|(
name|s
argument_list|)
condition|)
block|{
name|pFlags
operator||=
name|Pattern
operator|.
name|CASE_INSENSITIVE
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"MULTILINE"
operator|.
name|equals
argument_list|(
name|s
argument_list|)
condition|)
block|{
name|pFlags
operator||=
name|Pattern
operator|.
name|MULTILINE
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"DOTALL"
operator|.
name|equals
argument_list|(
name|s
argument_list|)
condition|)
block|{
name|pFlags
operator||=
name|Pattern
operator|.
name|DOTALL
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"UNICODE_CASE"
operator|.
name|equals
argument_list|(
name|s
argument_list|)
condition|)
block|{
name|pFlags
operator||=
name|Pattern
operator|.
name|UNICODE_CASE
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"CANON_EQ"
operator|.
name|equals
argument_list|(
name|s
argument_list|)
condition|)
block|{
name|pFlags
operator||=
name|Pattern
operator|.
name|CANON_EQ
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"UNIX_LINES"
operator|.
name|equals
argument_list|(
name|s
argument_list|)
condition|)
block|{
name|pFlags
operator||=
name|Pattern
operator|.
name|UNIX_LINES
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"LITERAL"
operator|.
name|equals
argument_list|(
name|s
argument_list|)
condition|)
block|{
name|pFlags
operator||=
name|Pattern
operator|.
name|LITERAL
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"COMMENTS"
operator|.
name|equals
argument_list|(
name|s
argument_list|)
condition|)
block|{
name|pFlags
operator||=
name|Pattern
operator|.
name|COMMENTS
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"UNICODE_CHAR_CLASS"
operator|.
name|equals
argument_list|(
name|s
argument_list|)
condition|)
block|{
name|pFlags
operator||=
name|UNICODE_CHARACTER_CLASS
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"Unknown regex flag ["
operator|+
name|s
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
return|return
name|pFlags
return|;
block|}
DECL|method|flagsToString
specifier|public
specifier|static
name|String
name|flagsToString
parameter_list|(
name|int
name|flags
parameter_list|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
name|flags
operator|&
name|Pattern
operator|.
name|CASE_INSENSITIVE
operator|)
operator|!=
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"CASE_INSENSITIVE|"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|(
name|flags
operator|&
name|Pattern
operator|.
name|MULTILINE
operator|)
operator|!=
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"MULTILINE|"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|(
name|flags
operator|&
name|Pattern
operator|.
name|DOTALL
operator|)
operator|!=
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"DOTALL|"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|(
name|flags
operator|&
name|Pattern
operator|.
name|UNICODE_CASE
operator|)
operator|!=
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"UNICODE_CASE|"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|(
name|flags
operator|&
name|Pattern
operator|.
name|CANON_EQ
operator|)
operator|!=
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"CANON_EQ|"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|(
name|flags
operator|&
name|Pattern
operator|.
name|UNIX_LINES
operator|)
operator|!=
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"UNIX_LINES|"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|(
name|flags
operator|&
name|Pattern
operator|.
name|LITERAL
operator|)
operator|!=
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"LITERAL|"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|(
name|flags
operator|&
name|Pattern
operator|.
name|COMMENTS
operator|)
operator|!=
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"COMMENTS|"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|(
name|flags
operator|&
name|UNICODE_CHARACTER_CLASS
operator|)
operator|!=
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"UNICODE_CHAR_CLASS|"
argument_list|)
expr_stmt|;
block|}
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

