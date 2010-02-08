begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.util
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|util
package|;
end_package

begin_import
import|import
name|org
operator|.
name|testng
operator|.
name|annotations
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
name|concurrent
operator|.
name|TimeUnit
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|MatcherAssert
operator|.
name|*
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
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|TimeValueTests
specifier|public
class|class
name|TimeValueTests
block|{
DECL|method|testSimple
annotation|@
name|Test
specifier|public
name|void
name|testSimple
parameter_list|()
block|{
name|assertThat
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
operator|.
name|toMillis
argument_list|(
literal|10
argument_list|)
argument_list|,
name|equalTo
argument_list|(
operator|new
name|TimeValue
argument_list|(
literal|10
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
operator|.
name|millis
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|TimeUnit
operator|.
name|MICROSECONDS
operator|.
name|toMicros
argument_list|(
literal|10
argument_list|)
argument_list|,
name|equalTo
argument_list|(
operator|new
name|TimeValue
argument_list|(
literal|10
argument_list|,
name|TimeUnit
operator|.
name|MICROSECONDS
argument_list|)
operator|.
name|micros
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
operator|.
name|toSeconds
argument_list|(
literal|10
argument_list|)
argument_list|,
name|equalTo
argument_list|(
operator|new
name|TimeValue
argument_list|(
literal|10
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
operator|.
name|seconds
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|TimeUnit
operator|.
name|MINUTES
operator|.
name|toMinutes
argument_list|(
literal|10
argument_list|)
argument_list|,
name|equalTo
argument_list|(
operator|new
name|TimeValue
argument_list|(
literal|10
argument_list|,
name|TimeUnit
operator|.
name|MINUTES
argument_list|)
operator|.
name|minutes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|TimeUnit
operator|.
name|HOURS
operator|.
name|toHours
argument_list|(
literal|10
argument_list|)
argument_list|,
name|equalTo
argument_list|(
operator|new
name|TimeValue
argument_list|(
literal|10
argument_list|,
name|TimeUnit
operator|.
name|HOURS
argument_list|)
operator|.
name|hours
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|TimeUnit
operator|.
name|DAYS
operator|.
name|toDays
argument_list|(
literal|10
argument_list|)
argument_list|,
name|equalTo
argument_list|(
operator|new
name|TimeValue
argument_list|(
literal|10
argument_list|,
name|TimeUnit
operator|.
name|DAYS
argument_list|)
operator|.
name|days
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testToString
annotation|@
name|Test
specifier|public
name|void
name|testToString
parameter_list|()
block|{
name|assertThat
argument_list|(
literal|"10ms"
argument_list|,
name|equalTo
argument_list|(
operator|new
name|TimeValue
argument_list|(
literal|10
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"1.5s"
argument_list|,
name|equalTo
argument_list|(
operator|new
name|TimeValue
argument_list|(
literal|1533
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"1.5m"
argument_list|,
name|equalTo
argument_list|(
operator|new
name|TimeValue
argument_list|(
literal|90
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"1.5h"
argument_list|,
name|equalTo
argument_list|(
operator|new
name|TimeValue
argument_list|(
literal|90
argument_list|,
name|TimeUnit
operator|.
name|MINUTES
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"1.5d"
argument_list|,
name|equalTo
argument_list|(
operator|new
name|TimeValue
argument_list|(
literal|36
argument_list|,
name|TimeUnit
operator|.
name|HOURS
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"1000d"
argument_list|,
name|equalTo
argument_list|(
operator|new
name|TimeValue
argument_list|(
literal|1000
argument_list|,
name|TimeUnit
operator|.
name|DAYS
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

