begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.unit
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|unit
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
name|ESTestCase
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
name|closeTo
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|DistanceUnitTests
specifier|public
class|class
name|DistanceUnitTests
extends|extends
name|ESTestCase
block|{
DECL|method|testSimpleDistanceUnit
specifier|public
name|void
name|testSimpleDistanceUnit
parameter_list|()
block|{
name|assertThat
argument_list|(
name|DistanceUnit
operator|.
name|KILOMETERS
operator|.
name|convert
argument_list|(
literal|10
argument_list|,
name|DistanceUnit
operator|.
name|MILES
argument_list|)
argument_list|,
name|closeTo
argument_list|(
literal|16.09344
argument_list|,
literal|0.001
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|DistanceUnit
operator|.
name|MILES
operator|.
name|convert
argument_list|(
literal|10
argument_list|,
name|DistanceUnit
operator|.
name|MILES
argument_list|)
argument_list|,
name|closeTo
argument_list|(
literal|10
argument_list|,
literal|0.001
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|DistanceUnit
operator|.
name|MILES
operator|.
name|convert
argument_list|(
literal|10
argument_list|,
name|DistanceUnit
operator|.
name|KILOMETERS
argument_list|)
argument_list|,
name|closeTo
argument_list|(
literal|6.21371192
argument_list|,
literal|0.001
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|DistanceUnit
operator|.
name|NAUTICALMILES
operator|.
name|convert
argument_list|(
literal|10
argument_list|,
name|DistanceUnit
operator|.
name|MILES
argument_list|)
argument_list|,
name|closeTo
argument_list|(
literal|8.689762
argument_list|,
literal|0.001
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|DistanceUnit
operator|.
name|KILOMETERS
operator|.
name|convert
argument_list|(
literal|10
argument_list|,
name|DistanceUnit
operator|.
name|KILOMETERS
argument_list|)
argument_list|,
name|closeTo
argument_list|(
literal|10
argument_list|,
literal|0.001
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|DistanceUnit
operator|.
name|KILOMETERS
operator|.
name|convert
argument_list|(
literal|10
argument_list|,
name|DistanceUnit
operator|.
name|METERS
argument_list|)
argument_list|,
name|closeTo
argument_list|(
literal|0.01
argument_list|,
literal|0.00001
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|DistanceUnit
operator|.
name|KILOMETERS
operator|.
name|convert
argument_list|(
literal|1000
argument_list|,
name|DistanceUnit
operator|.
name|METERS
argument_list|)
argument_list|,
name|closeTo
argument_list|(
literal|1
argument_list|,
literal|0.001
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|DistanceUnit
operator|.
name|METERS
operator|.
name|convert
argument_list|(
literal|1
argument_list|,
name|DistanceUnit
operator|.
name|KILOMETERS
argument_list|)
argument_list|,
name|closeTo
argument_list|(
literal|1000
argument_list|,
literal|0.001
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testDistanceUnitParsing
specifier|public
name|void
name|testDistanceUnitParsing
parameter_list|()
block|{
name|assertThat
argument_list|(
name|DistanceUnit
operator|.
name|Distance
operator|.
name|parseDistance
argument_list|(
literal|"50km"
argument_list|)
operator|.
name|unit
argument_list|,
name|equalTo
argument_list|(
name|DistanceUnit
operator|.
name|KILOMETERS
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|DistanceUnit
operator|.
name|Distance
operator|.
name|parseDistance
argument_list|(
literal|"500m"
argument_list|)
operator|.
name|unit
argument_list|,
name|equalTo
argument_list|(
name|DistanceUnit
operator|.
name|METERS
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|DistanceUnit
operator|.
name|Distance
operator|.
name|parseDistance
argument_list|(
literal|"51mi"
argument_list|)
operator|.
name|unit
argument_list|,
name|equalTo
argument_list|(
name|DistanceUnit
operator|.
name|MILES
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|DistanceUnit
operator|.
name|Distance
operator|.
name|parseDistance
argument_list|(
literal|"53nmi"
argument_list|)
operator|.
name|unit
argument_list|,
name|equalTo
argument_list|(
name|DistanceUnit
operator|.
name|NAUTICALMILES
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|DistanceUnit
operator|.
name|Distance
operator|.
name|parseDistance
argument_list|(
literal|"53NM"
argument_list|)
operator|.
name|unit
argument_list|,
name|equalTo
argument_list|(
name|DistanceUnit
operator|.
name|NAUTICALMILES
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|DistanceUnit
operator|.
name|Distance
operator|.
name|parseDistance
argument_list|(
literal|"52yd"
argument_list|)
operator|.
name|unit
argument_list|,
name|equalTo
argument_list|(
name|DistanceUnit
operator|.
name|YARD
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|DistanceUnit
operator|.
name|Distance
operator|.
name|parseDistance
argument_list|(
literal|"12in"
argument_list|)
operator|.
name|unit
argument_list|,
name|equalTo
argument_list|(
name|DistanceUnit
operator|.
name|INCH
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|DistanceUnit
operator|.
name|Distance
operator|.
name|parseDistance
argument_list|(
literal|"23mm"
argument_list|)
operator|.
name|unit
argument_list|,
name|equalTo
argument_list|(
name|DistanceUnit
operator|.
name|MILLIMETERS
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|DistanceUnit
operator|.
name|Distance
operator|.
name|parseDistance
argument_list|(
literal|"23cm"
argument_list|)
operator|.
name|unit
argument_list|,
name|equalTo
argument_list|(
name|DistanceUnit
operator|.
name|CENTIMETERS
argument_list|)
argument_list|)
expr_stmt|;
name|double
name|testValue
init|=
literal|12345.678
decl_stmt|;
for|for
control|(
name|DistanceUnit
name|unit
range|:
name|DistanceUnit
operator|.
name|values
argument_list|()
control|)
block|{
name|assertThat
argument_list|(
literal|"Unit can be parsed from '"
operator|+
name|unit
operator|.
name|toString
argument_list|()
operator|+
literal|"'"
argument_list|,
name|DistanceUnit
operator|.
name|fromString
argument_list|(
name|unit
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|unit
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"Unit can be parsed from '"
operator|+
name|testValue
operator|+
name|unit
operator|.
name|toString
argument_list|()
operator|+
literal|"'"
argument_list|,
name|DistanceUnit
operator|.
name|fromString
argument_list|(
name|unit
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|unit
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"Value can be parsed from '"
operator|+
name|testValue
operator|+
name|unit
operator|.
name|toString
argument_list|()
operator|+
literal|"'"
argument_list|,
name|DistanceUnit
operator|.
name|Distance
operator|.
name|parseDistance
argument_list|(
name|unit
operator|.
name|toString
argument_list|(
name|testValue
argument_list|)
argument_list|)
operator|.
name|value
argument_list|,
name|equalTo
argument_list|(
name|testValue
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

