begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.plugin.ingest.transport
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|plugin
operator|.
name|ingest
operator|.
name|transport
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
name|io
operator|.
name|stream
operator|.
name|BytesStreamOutput
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|Data
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
name|Collections
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

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|not
import|;
end_import

begin_class
DECL|class|TransportDataTests
specifier|public
class|class
name|TransportDataTests
extends|extends
name|ESTestCase
block|{
DECL|method|testEqualsAndHashcode
specifier|public
name|void
name|testEqualsAndHashcode
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|index
init|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|String
name|type
init|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|String
name|id
init|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|String
name|fieldName
init|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|String
name|fieldValue
init|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|TransportData
name|transportData
init|=
operator|new
name|TransportData
argument_list|(
operator|new
name|Data
argument_list|(
name|index
argument_list|,
name|type
argument_list|,
name|id
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
name|fieldName
argument_list|,
name|fieldValue
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|boolean
name|changed
init|=
literal|false
decl_stmt|;
name|String
name|otherIndex
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|otherIndex
operator|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|changed
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|otherIndex
operator|=
name|index
expr_stmt|;
block|}
name|String
name|otherType
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|otherType
operator|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|changed
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|otherType
operator|=
name|type
expr_stmt|;
block|}
name|String
name|otherId
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|otherId
operator|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|changed
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|otherId
operator|=
name|id
expr_stmt|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|document
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|document
operator|=
name|Collections
operator|.
name|singletonMap
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
argument_list|,
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
argument_list|)
expr_stmt|;
name|changed
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|document
operator|=
name|Collections
operator|.
name|singletonMap
argument_list|(
name|fieldName
argument_list|,
name|fieldValue
argument_list|)
expr_stmt|;
block|}
name|TransportData
name|otherTransportData
init|=
operator|new
name|TransportData
argument_list|(
operator|new
name|Data
argument_list|(
name|otherIndex
argument_list|,
name|otherType
argument_list|,
name|otherId
argument_list|,
name|document
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|changed
condition|)
block|{
name|assertThat
argument_list|(
name|transportData
argument_list|,
name|not
argument_list|(
name|equalTo
argument_list|(
name|otherTransportData
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|otherTransportData
argument_list|,
name|not
argument_list|(
name|equalTo
argument_list|(
name|transportData
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertThat
argument_list|(
name|transportData
argument_list|,
name|equalTo
argument_list|(
name|otherTransportData
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|otherTransportData
argument_list|,
name|equalTo
argument_list|(
name|transportData
argument_list|)
argument_list|)
expr_stmt|;
name|TransportData
name|thirdTransportData
init|=
operator|new
name|TransportData
argument_list|(
operator|new
name|Data
argument_list|(
name|index
argument_list|,
name|type
argument_list|,
name|id
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
name|fieldName
argument_list|,
name|fieldValue
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|thirdTransportData
argument_list|,
name|equalTo
argument_list|(
name|transportData
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|transportData
argument_list|,
name|equalTo
argument_list|(
name|thirdTransportData
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testSerialization
specifier|public
name|void
name|testSerialization
parameter_list|()
throws|throws
name|IOException
block|{
name|Data
name|data
init|=
operator|new
name|Data
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
argument_list|,
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
argument_list|,
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
argument_list|,
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|TransportData
name|transportData
init|=
operator|new
name|TransportData
argument_list|(
name|data
argument_list|)
decl_stmt|;
name|BytesStreamOutput
name|out
init|=
operator|new
name|BytesStreamOutput
argument_list|()
decl_stmt|;
name|transportData
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|StreamInput
name|streamInput
init|=
name|StreamInput
operator|.
name|wrap
argument_list|(
name|out
operator|.
name|bytes
argument_list|()
argument_list|)
decl_stmt|;
name|TransportData
name|otherTransportData
init|=
name|TransportData
operator|.
name|readTransportDataFrom
argument_list|(
name|streamInput
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|otherTransportData
argument_list|,
name|equalTo
argument_list|(
name|transportData
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

