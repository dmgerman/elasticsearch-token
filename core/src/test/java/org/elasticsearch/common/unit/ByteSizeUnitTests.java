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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|unit
operator|.
name|ByteSizeUnit
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
name|equalTo
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|ByteSizeUnitTests
specifier|public
class|class
name|ByteSizeUnitTests
extends|extends
name|ElasticsearchTestCase
block|{
annotation|@
name|Test
DECL|method|testBytes
specifier|public
name|void
name|testBytes
parameter_list|()
block|{
name|assertThat
argument_list|(
name|BYTES
operator|.
name|toBytes
argument_list|(
literal|1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|1l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|BYTES
operator|.
name|toKB
argument_list|(
literal|1024
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|1l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|BYTES
operator|.
name|toMB
argument_list|(
literal|1024
operator|*
literal|1024
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|1l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|BYTES
operator|.
name|toGB
argument_list|(
literal|1024
operator|*
literal|1024
operator|*
literal|1024
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|1l
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testKB
specifier|public
name|void
name|testKB
parameter_list|()
block|{
name|assertThat
argument_list|(
name|KB
operator|.
name|toBytes
argument_list|(
literal|1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|1024l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|KB
operator|.
name|toKB
argument_list|(
literal|1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|1l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|KB
operator|.
name|toMB
argument_list|(
literal|1024
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|1l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|KB
operator|.
name|toGB
argument_list|(
literal|1024
operator|*
literal|1024
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|1l
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testMB
specifier|public
name|void
name|testMB
parameter_list|()
block|{
name|assertThat
argument_list|(
name|MB
operator|.
name|toBytes
argument_list|(
literal|1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|1024l
operator|*
literal|1024
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|MB
operator|.
name|toKB
argument_list|(
literal|1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|1024l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|MB
operator|.
name|toMB
argument_list|(
literal|1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|1l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|MB
operator|.
name|toGB
argument_list|(
literal|1024
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|1l
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testGB
specifier|public
name|void
name|testGB
parameter_list|()
block|{
name|assertThat
argument_list|(
name|GB
operator|.
name|toBytes
argument_list|(
literal|1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|1024l
operator|*
literal|1024
operator|*
literal|1024
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|GB
operator|.
name|toKB
argument_list|(
literal|1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|1024l
operator|*
literal|1024
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|GB
operator|.
name|toMB
argument_list|(
literal|1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|1024l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|GB
operator|.
name|toGB
argument_list|(
literal|1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|1l
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testTB
specifier|public
name|void
name|testTB
parameter_list|()
block|{
name|assertThat
argument_list|(
name|TB
operator|.
name|toBytes
argument_list|(
literal|1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|1024l
operator|*
literal|1024
operator|*
literal|1024
operator|*
literal|1024
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|TB
operator|.
name|toKB
argument_list|(
literal|1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|1024l
operator|*
literal|1024
operator|*
literal|1024
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|TB
operator|.
name|toMB
argument_list|(
literal|1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|1024l
operator|*
literal|1024
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|TB
operator|.
name|toGB
argument_list|(
literal|1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|1024l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|TB
operator|.
name|toTB
argument_list|(
literal|1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|1l
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testPB
specifier|public
name|void
name|testPB
parameter_list|()
block|{
name|assertThat
argument_list|(
name|PB
operator|.
name|toBytes
argument_list|(
literal|1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|1024l
operator|*
literal|1024
operator|*
literal|1024
operator|*
literal|1024
operator|*
literal|1024
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|PB
operator|.
name|toKB
argument_list|(
literal|1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|1024l
operator|*
literal|1024
operator|*
literal|1024
operator|*
literal|1024
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|PB
operator|.
name|toMB
argument_list|(
literal|1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|1024l
operator|*
literal|1024
operator|*
literal|1024
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|PB
operator|.
name|toGB
argument_list|(
literal|1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|1024l
operator|*
literal|1024
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|PB
operator|.
name|toTB
argument_list|(
literal|1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|1024l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|PB
operator|.
name|toPB
argument_list|(
literal|1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|1l
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
