begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.fieldstats
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|fieldstats
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|bytes
operator|.
name|BytesArray
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ESTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|StreamsUtils
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|fieldstats
operator|.
name|IndexConstraint
operator|.
name|Comparison
operator|.
name|*
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|fieldstats
operator|.
name|IndexConstraint
operator|.
name|Property
operator|.
name|MAX
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|fieldstats
operator|.
name|IndexConstraint
operator|.
name|Property
operator|.
name|MIN
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
DECL|class|FieldStatsRequestTest
specifier|public
class|class
name|FieldStatsRequestTest
extends|extends
name|ESTestCase
block|{
DECL|method|testFieldsParsing
specifier|public
name|void
name|testFieldsParsing
parameter_list|()
throws|throws
name|Exception
block|{
name|byte
index|[]
name|data
init|=
name|StreamsUtils
operator|.
name|copyToBytesFromClasspath
argument_list|(
literal|"/org/elasticsearch/action/fieldstats/fieldstats-index-constraints-request.json"
argument_list|)
decl_stmt|;
name|FieldStatsRequest
name|request
init|=
operator|new
name|FieldStatsRequest
argument_list|()
decl_stmt|;
name|request
operator|.
name|source
argument_list|(
operator|new
name|BytesArray
argument_list|(
name|data
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getFields
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getFields
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"field1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getFields
argument_list|()
index|[
literal|1
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"field2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getFields
argument_list|()
index|[
literal|2
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"field3"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getFields
argument_list|()
index|[
literal|3
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"field4"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getFields
argument_list|()
index|[
literal|4
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"field5"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getIndexConstraints
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|6
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getIndexConstraints
argument_list|()
index|[
literal|0
index|]
operator|.
name|getField
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"field2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getIndexConstraints
argument_list|()
index|[
literal|0
index|]
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"9"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getIndexConstraints
argument_list|()
index|[
literal|0
index|]
operator|.
name|getProperty
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|MAX
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getIndexConstraints
argument_list|()
index|[
literal|0
index|]
operator|.
name|getComparison
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|GTE
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getIndexConstraints
argument_list|()
index|[
literal|1
index|]
operator|.
name|getField
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"field3"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getIndexConstraints
argument_list|()
index|[
literal|1
index|]
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"5"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getIndexConstraints
argument_list|()
index|[
literal|1
index|]
operator|.
name|getProperty
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|MIN
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getIndexConstraints
argument_list|()
index|[
literal|1
index|]
operator|.
name|getComparison
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|GT
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getIndexConstraints
argument_list|()
index|[
literal|2
index|]
operator|.
name|getField
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"field4"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getIndexConstraints
argument_list|()
index|[
literal|2
index|]
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"a"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getIndexConstraints
argument_list|()
index|[
literal|2
index|]
operator|.
name|getProperty
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|MIN
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getIndexConstraints
argument_list|()
index|[
literal|2
index|]
operator|.
name|getComparison
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|GTE
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getIndexConstraints
argument_list|()
index|[
literal|3
index|]
operator|.
name|getField
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"field4"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getIndexConstraints
argument_list|()
index|[
literal|3
index|]
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"g"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getIndexConstraints
argument_list|()
index|[
literal|3
index|]
operator|.
name|getProperty
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|MAX
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getIndexConstraints
argument_list|()
index|[
literal|3
index|]
operator|.
name|getComparison
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|LTE
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getIndexConstraints
argument_list|()
index|[
literal|4
index|]
operator|.
name|getField
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"field5"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getIndexConstraints
argument_list|()
index|[
literal|4
index|]
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getIndexConstraints
argument_list|()
index|[
literal|4
index|]
operator|.
name|getProperty
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|MAX
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getIndexConstraints
argument_list|()
index|[
literal|4
index|]
operator|.
name|getComparison
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|GT
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getIndexConstraints
argument_list|()
index|[
literal|5
index|]
operator|.
name|getField
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"field5"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getIndexConstraints
argument_list|()
index|[
literal|5
index|]
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"9"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getIndexConstraints
argument_list|()
index|[
literal|5
index|]
operator|.
name|getProperty
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|MAX
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|getIndexConstraints
argument_list|()
index|[
literal|5
index|]
operator|.
name|getComparison
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|LT
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

