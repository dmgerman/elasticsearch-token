begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.util
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|util
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|ArrayUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|RamUsageEstimator
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
name|hamcrest
operator|.
name|Matchers
operator|.
name|equalTo
import|;
end_import

begin_comment
comment|/**  * Tests for {@link SlicedObjectList}  */
end_comment

begin_class
DECL|class|SlicedObjectListTests
specifier|public
class|class
name|SlicedObjectListTests
extends|extends
name|ElasticsearchTestCase
block|{
DECL|class|TestList
specifier|public
class|class
name|TestList
extends|extends
name|SlicedObjectList
argument_list|<
name|Double
argument_list|>
block|{
DECL|method|TestList
specifier|public
name|TestList
parameter_list|(
name|int
name|capactiy
parameter_list|)
block|{
name|this
argument_list|(
operator|new
name|Double
index|[
name|capactiy
index|]
argument_list|,
literal|0
argument_list|,
name|capactiy
argument_list|)
expr_stmt|;
block|}
DECL|method|TestList
specifier|public
name|TestList
parameter_list|(
name|Double
index|[]
name|values
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|super
argument_list|(
name|values
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
DECL|method|TestList
specifier|public
name|TestList
parameter_list|(
name|Double
index|[]
name|values
parameter_list|)
block|{
name|super
argument_list|(
name|values
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|grow
specifier|public
name|void
name|grow
parameter_list|(
name|int
name|newLength
parameter_list|)
block|{
name|assertThat
argument_list|(
name|offset
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
comment|// NOTE: senseless if offset != 0
if|if
condition|(
name|values
operator|.
name|length
operator|>=
name|newLength
condition|)
block|{
return|return;
block|}
specifier|final
name|Double
index|[]
name|current
init|=
name|values
decl_stmt|;
name|values
operator|=
operator|new
name|Double
index|[
name|ArrayUtil
operator|.
name|oversize
argument_list|(
name|newLength
argument_list|,
name|RamUsageEstimator
operator|.
name|NUM_BYTES_OBJECT_REF
argument_list|)
index|]
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|current
argument_list|,
literal|0
argument_list|,
name|values
argument_list|,
literal|0
argument_list|,
name|current
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
DECL|method|testCapacity
specifier|public
name|void
name|testCapacity
parameter_list|()
block|{
name|TestList
name|list
init|=
operator|new
name|TestList
argument_list|(
literal|5
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|list
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
name|list
operator|.
name|offset
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|list
operator|.
name|values
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
name|list
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|list
operator|=
operator|new
name|TestList
argument_list|(
operator|new
name|Double
index|[
literal|10
index|]
argument_list|,
literal|5
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|list
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
name|list
operator|.
name|offset
argument_list|,
name|equalTo
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|list
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|list
operator|.
name|values
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|10
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testGrow
specifier|public
name|void
name|testGrow
parameter_list|()
block|{
name|TestList
name|list
init|=
operator|new
name|TestList
argument_list|(
literal|5
argument_list|)
decl_stmt|;
name|list
operator|.
name|length
operator|=
literal|1000
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|list
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|list
operator|.
name|grow
argument_list|(
name|i
operator|+
literal|1
argument_list|)
expr_stmt|;
name|list
operator|.
name|values
index|[
name|i
index|]
operator|=
operator|(
operator|(
name|double
operator|)
name|i
operator|)
expr_stmt|;
block|}
name|int
name|expected
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Double
name|d
range|:
name|list
control|)
block|{
name|assertThat
argument_list|(
operator|(
name|double
operator|)
name|expected
operator|++
argument_list|,
name|equalTo
argument_list|(
name|d
argument_list|)
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|list
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|assertThat
argument_list|(
operator|(
name|double
operator|)
name|i
argument_list|,
name|equalTo
argument_list|(
name|list
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|list
operator|.
name|offset
init|;
name|i
operator|<
name|list
operator|.
name|offset
operator|+
name|list
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|assertThat
argument_list|(
operator|(
name|double
operator|)
name|count
operator|++
argument_list|,
name|equalTo
argument_list|(
name|list
operator|.
name|values
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
DECL|method|testIndexOf
specifier|public
name|void
name|testIndexOf
parameter_list|()
block|{
name|TestList
name|list
init|=
operator|new
name|TestList
argument_list|(
literal|5
argument_list|)
decl_stmt|;
name|list
operator|.
name|length
operator|=
literal|1000
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|list
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|list
operator|.
name|grow
argument_list|(
name|i
operator|+
literal|1
argument_list|)
expr_stmt|;
name|list
operator|.
name|values
index|[
name|i
index|]
operator|=
operator|(
operator|(
name|double
operator|)
name|i
operator|%
literal|100
operator|)
expr_stmt|;
block|}
name|assertThat
argument_list|(
literal|999
argument_list|,
name|equalTo
argument_list|(
name|list
operator|.
name|lastIndexOf
argument_list|(
literal|99.0d
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|99
argument_list|,
name|equalTo
argument_list|(
name|list
operator|.
name|indexOf
argument_list|(
literal|99.0d
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|-
literal|1
argument_list|,
name|equalTo
argument_list|(
name|list
operator|.
name|lastIndexOf
argument_list|(
literal|100.0d
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|-
literal|1
argument_list|,
name|equalTo
argument_list|(
name|list
operator|.
name|indexOf
argument_list|(
literal|100.0d
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testIsEmpty
specifier|public
name|void
name|testIsEmpty
parameter_list|()
block|{
name|TestList
name|list
init|=
operator|new
name|TestList
argument_list|(
literal|5
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
literal|false
argument_list|,
name|equalTo
argument_list|(
name|list
operator|.
name|isEmpty
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|list
operator|.
name|length
operator|=
literal|0
expr_stmt|;
name|assertThat
argument_list|(
literal|true
argument_list|,
name|equalTo
argument_list|(
name|list
operator|.
name|isEmpty
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testSet
specifier|public
name|void
name|testSet
parameter_list|()
block|{
name|TestList
name|list
init|=
operator|new
name|TestList
argument_list|(
literal|5
argument_list|)
decl_stmt|;
try|try
block|{
name|list
operator|.
name|set
argument_list|(
literal|0
argument_list|,
operator|(
name|double
operator|)
literal|4
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedOperationException
name|ex
parameter_list|)
block|{         }
try|try
block|{
name|list
operator|.
name|add
argument_list|(
operator|(
name|double
operator|)
literal|4
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedOperationException
name|ex
parameter_list|)
block|{         }
block|}
annotation|@
name|Test
DECL|method|testToString
specifier|public
name|void
name|testToString
parameter_list|()
block|{
name|TestList
name|list
init|=
operator|new
name|TestList
argument_list|(
literal|5
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
literal|"[null, null, null, null, null]"
argument_list|,
name|equalTo
argument_list|(
name|list
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|list
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|list
operator|.
name|grow
argument_list|(
name|i
operator|+
literal|1
argument_list|)
expr_stmt|;
name|list
operator|.
name|values
index|[
name|i
index|]
operator|=
operator|(
operator|(
name|double
operator|)
name|i
operator|)
expr_stmt|;
block|}
name|assertThat
argument_list|(
literal|"[0.0, 1.0, 2.0, 3.0, 4.0]"
argument_list|,
name|equalTo
argument_list|(
name|list
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

