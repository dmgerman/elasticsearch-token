begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|test
operator|.
name|ElasticsearchTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
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
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|equalTo
import|;
end_import

begin_class
DECL|class|RegexTests
specifier|public
class|class
name|RegexTests
extends|extends
name|ElasticsearchTestCase
block|{
annotation|@
name|Test
DECL|method|testFlags
specifier|public
name|void
name|testFlags
parameter_list|()
block|{
name|String
index|[]
name|supportedFlags
init|=
operator|new
name|String
index|[]
block|{
literal|"CASE_INSENSITIVE"
block|,
literal|"MULTILINE"
block|,
literal|"DOTALL"
block|,
literal|"UNICODE_CASE"
block|,
literal|"CANON_EQ"
block|,
literal|"UNIX_LINES"
block|,
literal|"LITERAL"
block|,
literal|"COMMENTS"
block|,
literal|"UNICODE_CHAR_CLASS"
block|,
literal|"UNICODE_CHARACTER_CLASS"
block|}
decl_stmt|;
name|int
index|[]
name|flags
init|=
operator|new
name|int
index|[]
block|{
name|Pattern
operator|.
name|CASE_INSENSITIVE
block|,
name|Pattern
operator|.
name|MULTILINE
block|,
name|Pattern
operator|.
name|DOTALL
block|,
name|Pattern
operator|.
name|UNICODE_CASE
block|,
name|Pattern
operator|.
name|CANON_EQ
block|,
name|Pattern
operator|.
name|UNIX_LINES
block|,
name|Pattern
operator|.
name|LITERAL
block|,
name|Pattern
operator|.
name|COMMENTS
block|,
name|Regex
operator|.
name|UNICODE_CHARACTER_CLASS
block|}
decl_stmt|;
name|Random
name|random
init|=
name|getRandom
argument_list|()
decl_stmt|;
name|int
name|num
init|=
literal|10
operator|+
name|random
operator|.
name|nextInt
argument_list|(
literal|100
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
name|num
condition|;
name|i
operator|++
control|)
block|{
name|int
name|numFlags
init|=
name|random
operator|.
name|nextInt
argument_list|(
name|flags
operator|.
name|length
operator|+
literal|1
argument_list|)
decl_stmt|;
name|int
name|current
init|=
literal|0
decl_stmt|;
name|StringBuilder
name|builder
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|numFlags
condition|;
name|j
operator|++
control|)
block|{
name|int
name|index
init|=
name|random
operator|.
name|nextInt
argument_list|(
name|flags
operator|.
name|length
argument_list|)
decl_stmt|;
name|current
operator||=
name|flags
index|[
name|index
index|]
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|supportedFlags
index|[
name|index
index|]
argument_list|)
expr_stmt|;
if|if
condition|(
name|j
operator|<
name|numFlags
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
literal|"|"
argument_list|)
expr_stmt|;
block|}
block|}
name|String
name|flagsToString
init|=
name|Regex
operator|.
name|flagsToString
argument_list|(
name|current
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|Regex
operator|.
name|flagsFromString
argument_list|(
name|builder
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|current
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Regex
operator|.
name|flagsFromString
argument_list|(
name|builder
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|Regex
operator|.
name|flagsFromString
argument_list|(
name|flagsToString
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|Pattern
operator|.
name|compile
argument_list|(
literal|"\\w\\d{1,2}"
argument_list|,
name|current
argument_list|)
expr_stmt|;
comment|// accepts the flags?
block|}
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|1000
argument_list|)
DECL|method|testDoubleWildcardMatch
specifier|public
name|void
name|testDoubleWildcardMatch
parameter_list|()
block|{
name|assertTrue
argument_list|(
name|Regex
operator|.
name|simpleMatch
argument_list|(
literal|"ddd"
argument_list|,
literal|"ddd"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Regex
operator|.
name|simpleMatch
argument_list|(
literal|"d*d*d"
argument_list|,
literal|"dadd"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Regex
operator|.
name|simpleMatch
argument_list|(
literal|"**ddd"
argument_list|,
literal|"dddd"
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|Regex
operator|.
name|simpleMatch
argument_list|(
literal|"**ddd"
argument_list|,
literal|"fff"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Regex
operator|.
name|simpleMatch
argument_list|(
literal|"fff*ddd"
argument_list|,
literal|"fffabcddd"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Regex
operator|.
name|simpleMatch
argument_list|(
literal|"fff**ddd"
argument_list|,
literal|"fffabcddd"
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|Regex
operator|.
name|simpleMatch
argument_list|(
literal|"fff**ddd"
argument_list|,
literal|"fffabcdd"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Regex
operator|.
name|simpleMatch
argument_list|(
literal|"fff*******ddd"
argument_list|,
literal|"fffabcddd"
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|Regex
operator|.
name|simpleMatch
argument_list|(
literal|"fff******ddd"
argument_list|,
literal|"fffabcdd"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

