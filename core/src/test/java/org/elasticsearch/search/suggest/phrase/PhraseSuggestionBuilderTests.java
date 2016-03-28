begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.suggest.phrase
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|phrase
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|Template
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|AbstractSuggestionBuilderTestCase
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
name|HashMap
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

begin_class
DECL|class|PhraseSuggestionBuilderTests
specifier|public
class|class
name|PhraseSuggestionBuilderTests
extends|extends
name|AbstractSuggestionBuilderTestCase
argument_list|<
name|PhraseSuggestionBuilder
argument_list|>
block|{
annotation|@
name|Override
DECL|method|randomSuggestionBuilder
specifier|protected
name|PhraseSuggestionBuilder
name|randomSuggestionBuilder
parameter_list|()
block|{
return|return
name|randomPhraseSuggestionBuilder
argument_list|()
return|;
block|}
DECL|method|randomPhraseSuggestionBuilder
specifier|public
specifier|static
name|PhraseSuggestionBuilder
name|randomPhraseSuggestionBuilder
parameter_list|()
block|{
name|PhraseSuggestionBuilder
name|testBuilder
init|=
operator|new
name|PhraseSuggestionBuilder
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|2
argument_list|,
literal|20
argument_list|)
argument_list|)
decl_stmt|;
name|setCommonPropertiesOnRandomBuilder
argument_list|(
name|testBuilder
argument_list|)
expr_stmt|;
name|maybeSet
argument_list|(
name|testBuilder
operator|::
name|maxErrors
argument_list|,
name|randomFloat
argument_list|()
argument_list|)
expr_stmt|;
name|maybeSet
argument_list|(
name|testBuilder
operator|::
name|separator
argument_list|,
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
argument_list|)
expr_stmt|;
name|maybeSet
argument_list|(
name|testBuilder
operator|::
name|realWordErrorLikelihood
argument_list|,
name|randomFloat
argument_list|()
argument_list|)
expr_stmt|;
name|maybeSet
argument_list|(
name|testBuilder
operator|::
name|confidence
argument_list|,
name|randomFloat
argument_list|()
argument_list|)
expr_stmt|;
name|maybeSet
argument_list|(
name|testBuilder
operator|::
name|collateQuery
argument_list|,
name|randomAsciiOfLengthBetween
argument_list|(
literal|3
argument_list|,
literal|20
argument_list|)
argument_list|)
expr_stmt|;
comment|// collate query prune and parameters will only be used when query is set
if|if
condition|(
name|testBuilder
operator|.
name|collateQuery
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|maybeSet
argument_list|(
name|testBuilder
operator|::
name|collatePrune
argument_list|,
name|randomBoolean
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|collateParams
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|int
name|numParams
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
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
name|numParams
condition|;
name|i
operator|++
control|)
block|{
name|collateParams
operator|.
name|put
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|5
argument_list|)
argument_list|,
name|randomAsciiOfLength
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|testBuilder
operator|.
name|collateParams
argument_list|(
name|collateParams
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
comment|// preTag, postTag
name|testBuilder
operator|.
name|highlight
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|3
argument_list|,
literal|20
argument_list|)
argument_list|,
name|randomAsciiOfLengthBetween
argument_list|(
literal|3
argument_list|,
literal|20
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|maybeSet
argument_list|(
name|testBuilder
operator|::
name|gramSize
argument_list|,
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|maybeSet
argument_list|(
name|testBuilder
operator|::
name|forceUnigrams
argument_list|,
name|randomBoolean
argument_list|()
argument_list|)
expr_stmt|;
name|maybeSet
argument_list|(
name|testBuilder
operator|::
name|tokenLimit
argument_list|,
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|20
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|testBuilder
operator|.
name|smoothingModel
argument_list|(
name|randomSmoothingModel
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|int
name|numGenerators
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
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
name|numGenerators
condition|;
name|i
operator|++
control|)
block|{
name|testBuilder
operator|.
name|addCandidateGenerator
argument_list|(
name|DirectCandidateGeneratorTests
operator|.
name|randomCandidateGenerator
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|testBuilder
return|;
block|}
DECL|method|randomSmoothingModel
specifier|private
specifier|static
name|SmoothingModel
name|randomSmoothingModel
parameter_list|()
block|{
name|SmoothingModel
name|model
init|=
literal|null
decl_stmt|;
switch|switch
condition|(
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|2
argument_list|)
condition|)
block|{
case|case
literal|0
case|:
name|model
operator|=
name|LaplaceModelTests
operator|.
name|createRandomModel
argument_list|()
expr_stmt|;
break|break;
case|case
literal|1
case|:
name|model
operator|=
name|StupidBackoffModelTests
operator|.
name|createRandomModel
argument_list|()
expr_stmt|;
break|break;
case|case
literal|2
case|:
name|model
operator|=
name|LinearInterpolationModelTests
operator|.
name|createRandomModel
argument_list|()
expr_stmt|;
break|break;
block|}
return|return
name|model
return|;
block|}
annotation|@
name|Override
DECL|method|mutateSpecificParameters
specifier|protected
name|void
name|mutateSpecificParameters
parameter_list|(
name|PhraseSuggestionBuilder
name|builder
parameter_list|)
throws|throws
name|IOException
block|{
switch|switch
condition|(
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|12
argument_list|)
condition|)
block|{
case|case
literal|0
case|:
name|builder
operator|.
name|maxErrors
argument_list|(
name|randomValueOtherThan
argument_list|(
name|builder
operator|.
name|maxErrors
argument_list|()
argument_list|,
parameter_list|()
lambda|->
name|randomFloat
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|1
case|:
name|builder
operator|.
name|realWordErrorLikelihood
argument_list|(
name|randomValueOtherThan
argument_list|(
name|builder
operator|.
name|realWordErrorLikelihood
argument_list|()
argument_list|,
parameter_list|()
lambda|->
name|randomFloat
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|2
case|:
name|builder
operator|.
name|confidence
argument_list|(
name|randomValueOtherThan
argument_list|(
name|builder
operator|.
name|confidence
argument_list|()
argument_list|,
parameter_list|()
lambda|->
name|randomFloat
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|3
case|:
name|builder
operator|.
name|gramSize
argument_list|(
name|randomValueOtherThan
argument_list|(
name|builder
operator|.
name|gramSize
argument_list|()
argument_list|,
parameter_list|()
lambda|->
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|4
case|:
name|builder
operator|.
name|tokenLimit
argument_list|(
name|randomValueOtherThan
argument_list|(
name|builder
operator|.
name|tokenLimit
argument_list|()
argument_list|,
parameter_list|()
lambda|->
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|20
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|5
case|:
name|builder
operator|.
name|separator
argument_list|(
name|randomValueOtherThan
argument_list|(
name|builder
operator|.
name|separator
argument_list|()
argument_list|,
parameter_list|()
lambda|->
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|6
case|:
name|Template
name|collateQuery
init|=
name|builder
operator|.
name|collateQuery
argument_list|()
decl_stmt|;
if|if
condition|(
name|collateQuery
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|collateQuery
argument_list|(
name|randomValueOtherThan
argument_list|(
name|collateQuery
operator|.
name|getScript
argument_list|()
argument_list|,
parameter_list|()
lambda|->
name|randomAsciiOfLengthBetween
argument_list|(
literal|3
argument_list|,
literal|20
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|builder
operator|.
name|collateQuery
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|3
argument_list|,
literal|20
argument_list|)
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
literal|7
case|:
name|builder
operator|.
name|collatePrune
argument_list|(
name|builder
operator|.
name|collatePrune
argument_list|()
operator|==
literal|null
condition|?
name|randomBoolean
argument_list|()
else|:
operator|!
name|builder
operator|.
name|collatePrune
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
literal|8
case|:
comment|// preTag, postTag
name|String
name|currentPre
init|=
name|builder
operator|.
name|preTag
argument_list|()
decl_stmt|;
if|if
condition|(
name|currentPre
operator|!=
literal|null
condition|)
block|{
comment|// simply double both values
name|builder
operator|.
name|highlight
argument_list|(
name|builder
operator|.
name|preTag
argument_list|()
operator|+
name|builder
operator|.
name|preTag
argument_list|()
argument_list|,
name|builder
operator|.
name|postTag
argument_list|()
operator|+
name|builder
operator|.
name|postTag
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|builder
operator|.
name|highlight
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|3
argument_list|,
literal|20
argument_list|)
argument_list|,
name|randomAsciiOfLengthBetween
argument_list|(
literal|3
argument_list|,
literal|20
argument_list|)
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
literal|9
case|:
name|builder
operator|.
name|forceUnigrams
argument_list|(
name|builder
operator|.
name|forceUnigrams
argument_list|()
operator|==
literal|null
condition|?
name|randomBoolean
argument_list|()
else|:
operator|!
name|builder
operator|.
name|forceUnigrams
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
literal|10
case|:
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|collateParams
init|=
name|builder
operator|.
name|collateParams
argument_list|()
operator|==
literal|null
condition|?
operator|new
name|HashMap
argument_list|<>
argument_list|(
literal|1
argument_list|)
else|:
name|builder
operator|.
name|collateParams
argument_list|()
decl_stmt|;
name|collateParams
operator|.
name|put
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|5
argument_list|)
argument_list|,
name|randomAsciiOfLength
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|collateParams
argument_list|(
name|collateParams
argument_list|)
expr_stmt|;
break|break;
case|case
literal|11
case|:
name|builder
operator|.
name|smoothingModel
argument_list|(
name|randomValueOtherThan
argument_list|(
name|builder
operator|.
name|smoothingModel
argument_list|()
argument_list|,
name|PhraseSuggestionBuilderTests
operator|::
name|randomSmoothingModel
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|12
case|:
name|builder
operator|.
name|addCandidateGenerator
argument_list|(
name|DirectCandidateGeneratorTests
operator|.
name|randomCandidateGenerator
argument_list|()
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
DECL|method|testInvalidParameters
specifier|public
name|void
name|testInvalidParameters
parameter_list|()
throws|throws
name|IOException
block|{
comment|// test missing field name
name|Exception
name|e
init|=
name|expectThrows
argument_list|(
name|NullPointerException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
operator|new
name|PhraseSuggestionBuilder
argument_list|(
operator|(
name|String
operator|)
literal|null
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"suggestion requires a field name"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
comment|// test empty field name
name|e
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
operator|new
name|PhraseSuggestionBuilder
argument_list|(
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"suggestion field name is empty"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|PhraseSuggestionBuilder
name|builder
init|=
operator|new
name|PhraseSuggestionBuilder
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|2
argument_list|,
literal|20
argument_list|)
argument_list|)
decl_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|builder
operator|.
name|gramSize
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"gramSize must be>= 1"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|builder
operator|.
name|gramSize
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"gramSize must be>= 1"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|builder
operator|.
name|maxErrors
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"max_error must be> 0.0"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|NullPointerException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|builder
operator|.
name|separator
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"separator cannot be set to null"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|builder
operator|.
name|realWordErrorLikelihood
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"real_word_error_likelihood must be> 0.0"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|builder
operator|.
name|confidence
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"confidence must be>= 0.0"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|builder
operator|.
name|tokenLimit
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"token_limit must be>= 1"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|builder
operator|.
name|highlight
argument_list|(
literal|null
argument_list|,
literal|"</b>"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Pre and post tag must both be null or both not be null."
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|builder
operator|.
name|highlight
argument_list|(
literal|"<b>"
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Pre and post tag must both be null or both not be null."
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

