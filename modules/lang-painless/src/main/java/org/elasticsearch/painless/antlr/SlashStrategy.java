begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.painless.antlr
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|painless
operator|.
name|antlr
package|;
end_package

begin_import
import|import
name|org
operator|.
name|antlr
operator|.
name|v4
operator|.
name|runtime
operator|.
name|Token
import|;
end_import

begin_import
import|import
name|org
operator|.
name|antlr
operator|.
name|v4
operator|.
name|runtime
operator|.
name|TokenFactory
import|;
end_import

begin_comment
comment|/**  * Utility to figure out if a {@code /} is division or the start of a regex literal.  */
end_comment

begin_class
DECL|class|SlashStrategy
specifier|public
class|class
name|SlashStrategy
block|{
DECL|method|slashIsRegex
specifier|public
specifier|static
name|boolean
name|slashIsRegex
parameter_list|(
name|TokenFactory
argument_list|<
name|?
argument_list|>
name|factory
parameter_list|)
block|{
name|StashingTokenFactory
argument_list|<
name|?
argument_list|>
name|stashingFactory
init|=
operator|(
name|StashingTokenFactory
argument_list|<
name|?
argument_list|>
operator|)
name|factory
decl_stmt|;
name|Token
name|lastToken
init|=
name|stashingFactory
operator|.
name|getLastToken
argument_list|()
decl_stmt|;
if|if
condition|(
name|lastToken
operator|==
literal|null
condition|)
block|{
return|return
literal|true
return|;
block|}
switch|switch
condition|(
name|lastToken
operator|.
name|getType
argument_list|()
condition|)
block|{
case|case
name|PainlessLexer
operator|.
name|RBRACE
case|:
case|case
name|PainlessLexer
operator|.
name|RP
case|:
case|case
name|PainlessLexer
operator|.
name|OCTAL
case|:
case|case
name|PainlessLexer
operator|.
name|HEX
case|:
case|case
name|PainlessLexer
operator|.
name|INTEGER
case|:
case|case
name|PainlessLexer
operator|.
name|DECIMAL
case|:
case|case
name|PainlessLexer
operator|.
name|ID
case|:
case|case
name|PainlessLexer
operator|.
name|DOTINTEGER
case|:
case|case
name|PainlessLexer
operator|.
name|DOTID
case|:
return|return
literal|false
return|;
default|default:
return|return
literal|true
return|;
block|}
block|}
block|}
end_class

end_unit

