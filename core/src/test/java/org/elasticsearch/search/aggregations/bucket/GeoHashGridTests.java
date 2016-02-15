begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|bucket
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|BaseAggregationTestCase
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
name|aggregations
operator|.
name|bucket
operator|.
name|geogrid
operator|.
name|GeoGridAggregatorBuilder
import|;
end_import

begin_class
DECL|class|GeoHashGridTests
specifier|public
class|class
name|GeoHashGridTests
extends|extends
name|BaseAggregationTestCase
argument_list|<
name|GeoGridAggregatorBuilder
argument_list|>
block|{
annotation|@
name|Override
DECL|method|createTestAggregatorBuilder
specifier|protected
name|GeoGridAggregatorBuilder
name|createTestAggregatorBuilder
parameter_list|()
block|{
name|String
name|name
init|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|3
argument_list|,
literal|20
argument_list|)
decl_stmt|;
name|GeoGridAggregatorBuilder
name|factory
init|=
operator|new
name|GeoGridAggregatorBuilder
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|int
name|precision
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|12
argument_list|)
decl_stmt|;
name|factory
operator|.
name|precision
argument_list|(
name|precision
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
name|size
init|=
name|randomInt
argument_list|(
literal|5
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|size
condition|)
block|{
case|case
literal|0
case|:
break|break;
case|case
literal|1
case|:
case|case
literal|2
case|:
case|case
literal|3
case|:
case|case
literal|4
case|:
name|size
operator|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
break|break;
block|}
name|factory
operator|.
name|size
argument_list|(
name|size
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
name|shardSize
init|=
name|randomInt
argument_list|(
literal|5
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|shardSize
condition|)
block|{
case|case
literal|0
case|:
break|break;
case|case
literal|1
case|:
case|case
literal|2
case|:
case|case
literal|3
case|:
case|case
literal|4
case|:
name|shardSize
operator|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
break|break;
block|}
name|factory
operator|.
name|shardSize
argument_list|(
name|shardSize
argument_list|)
expr_stmt|;
block|}
return|return
name|factory
return|;
block|}
block|}
end_class

end_unit

