begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.query
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
package|;
end_package

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|generators
operator|.
name|RandomInts
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

begin_comment
comment|/**  * Utility class for creating random QueryBuilders.  * So far only leaf queries like {@link MatchAllQueryBuilder}, {@link TermQueryBuilder} or  * {@link IdsQueryBuilder} are returned.  */
end_comment

begin_class
DECL|class|RandomQueryBuilder
specifier|public
class|class
name|RandomQueryBuilder
block|{
comment|/**      * @param r random seed      * @return a random {@link QueryBuilder}      */
DECL|method|create
specifier|public
specifier|static
name|QueryBuilder
name|create
parameter_list|(
name|Random
name|r
parameter_list|)
block|{
name|QueryBuilder
name|query
init|=
literal|null
decl_stmt|;
switch|switch
condition|(
name|RandomInts
operator|.
name|randomIntBetween
argument_list|(
name|r
argument_list|,
literal|0
argument_list|,
literal|2
argument_list|)
condition|)
block|{
case|case
literal|0
case|:
return|return
operator|new
name|MatchAllQueryBuilderTest
argument_list|()
operator|.
name|createTestQueryBuilder
argument_list|()
return|;
case|case
literal|1
case|:
return|return
operator|new
name|TermQueryBuilderTest
argument_list|()
operator|.
name|createTestQueryBuilder
argument_list|()
return|;
case|case
literal|2
case|:
return|return
operator|new
name|IdsQueryBuilderTest
argument_list|()
operator|.
name|createTestQueryBuilder
argument_list|()
return|;
block|}
return|return
name|query
return|;
block|}
block|}
end_class

end_unit

