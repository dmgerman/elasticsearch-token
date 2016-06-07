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
name|CharStream
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
name|LexerNoViableAltException
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
name|misc
operator|.
name|Interval
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|painless
operator|.
name|Location
import|;
end_import

begin_comment
comment|/**  * A lexer that will override the default error behavior to fail on the first error.  */
end_comment

begin_class
DECL|class|ErrorHandlingLexer
specifier|final
class|class
name|ErrorHandlingLexer
extends|extends
name|PainlessLexer
block|{
DECL|field|sourceName
specifier|final
name|String
name|sourceName
decl_stmt|;
DECL|method|ErrorHandlingLexer
name|ErrorHandlingLexer
parameter_list|(
name|CharStream
name|charStream
parameter_list|,
name|String
name|sourceName
parameter_list|)
block|{
name|super
argument_list|(
name|charStream
argument_list|)
expr_stmt|;
name|this
operator|.
name|sourceName
operator|=
name|sourceName
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|recover
specifier|public
name|void
name|recover
parameter_list|(
specifier|final
name|LexerNoViableAltException
name|lnvae
parameter_list|)
block|{
specifier|final
name|CharStream
name|charStream
init|=
name|lnvae
operator|.
name|getInputStream
argument_list|()
decl_stmt|;
specifier|final
name|int
name|startIndex
init|=
name|lnvae
operator|.
name|getStartIndex
argument_list|()
decl_stmt|;
specifier|final
name|String
name|text
init|=
name|charStream
operator|.
name|getText
argument_list|(
name|Interval
operator|.
name|of
argument_list|(
name|startIndex
argument_list|,
name|charStream
operator|.
name|index
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|Location
name|location
init|=
operator|new
name|Location
argument_list|(
name|sourceName
argument_list|,
name|_tokenStartCharIndex
argument_list|)
decl_stmt|;
throw|throw
name|location
operator|.
name|createError
argument_list|(
operator|new
name|IllegalArgumentException
argument_list|(
literal|"unexpected character ["
operator|+
name|getErrorDisplay
argument_list|(
name|text
argument_list|)
operator|+
literal|"]."
argument_list|,
name|lnvae
argument_list|)
argument_list|)
throw|;
block|}
block|}
end_class

end_unit
