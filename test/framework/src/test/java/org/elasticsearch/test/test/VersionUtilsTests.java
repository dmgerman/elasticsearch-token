begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.test
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|test
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|Version
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
name|VersionUtils
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_class
DECL|class|VersionUtilsTests
specifier|public
class|class
name|VersionUtilsTests
extends|extends
name|ESTestCase
block|{
DECL|method|testAllVersionsSorted
specifier|public
name|void
name|testAllVersionsSorted
parameter_list|()
block|{
name|List
argument_list|<
name|Version
argument_list|>
name|allVersions
init|=
name|VersionUtils
operator|.
name|allVersions
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|,
name|j
init|=
literal|1
init|;
name|j
operator|<
name|allVersions
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
operator|,
operator|++
name|j
control|)
block|{
name|assertTrue
argument_list|(
name|allVersions
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|before
argument_list|(
name|allVersions
operator|.
name|get
argument_list|(
name|j
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testRandomVersionBetween
specifier|public
name|void
name|testRandomVersionBetween
parameter_list|()
block|{
comment|// full range
name|Version
name|got
init|=
name|VersionUtils
operator|.
name|randomVersionBetween
argument_list|(
name|random
argument_list|()
argument_list|,
name|VersionUtils
operator|.
name|getFirstVersion
argument_list|()
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|got
operator|.
name|onOrAfter
argument_list|(
name|VersionUtils
operator|.
name|getFirstVersion
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|got
operator|.
name|onOrBefore
argument_list|(
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|)
expr_stmt|;
name|got
operator|=
name|VersionUtils
operator|.
name|randomVersionBetween
argument_list|(
name|random
argument_list|()
argument_list|,
literal|null
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|got
operator|.
name|onOrAfter
argument_list|(
name|VersionUtils
operator|.
name|getFirstVersion
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|got
operator|.
name|onOrBefore
argument_list|(
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|)
expr_stmt|;
name|got
operator|=
name|VersionUtils
operator|.
name|randomVersionBetween
argument_list|(
name|random
argument_list|()
argument_list|,
name|VersionUtils
operator|.
name|getFirstVersion
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|got
operator|.
name|onOrAfter
argument_list|(
name|VersionUtils
operator|.
name|getFirstVersion
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|got
operator|.
name|onOrBefore
argument_list|(
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|)
expr_stmt|;
comment|// sub range
name|got
operator|=
name|VersionUtils
operator|.
name|randomVersionBetween
argument_list|(
name|random
argument_list|()
argument_list|,
name|Version
operator|.
name|V_2_0_0
argument_list|,
name|Version
operator|.
name|V_5_0_0_alpha1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|got
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_2_0_0
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|got
operator|.
name|onOrBefore
argument_list|(
name|Version
operator|.
name|V_5_0_0_alpha1
argument_list|)
argument_list|)
expr_stmt|;
comment|// unbounded lower
name|got
operator|=
name|VersionUtils
operator|.
name|randomVersionBetween
argument_list|(
name|random
argument_list|()
argument_list|,
literal|null
argument_list|,
name|Version
operator|.
name|V_5_0_0_alpha1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|got
operator|.
name|onOrAfter
argument_list|(
name|VersionUtils
operator|.
name|getFirstVersion
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|got
operator|.
name|onOrBefore
argument_list|(
name|Version
operator|.
name|V_5_0_0_alpha1
argument_list|)
argument_list|)
expr_stmt|;
name|got
operator|=
name|VersionUtils
operator|.
name|randomVersionBetween
argument_list|(
name|random
argument_list|()
argument_list|,
literal|null
argument_list|,
name|VersionUtils
operator|.
name|allVersions
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|got
operator|.
name|onOrAfter
argument_list|(
name|VersionUtils
operator|.
name|getFirstVersion
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|got
operator|.
name|onOrBefore
argument_list|(
name|VersionUtils
operator|.
name|allVersions
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// unbounded upper
name|got
operator|=
name|VersionUtils
operator|.
name|randomVersionBetween
argument_list|(
name|random
argument_list|()
argument_list|,
name|Version
operator|.
name|V_2_0_0
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|got
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_2_0_0
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|got
operator|.
name|onOrBefore
argument_list|(
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|)
expr_stmt|;
name|got
operator|=
name|VersionUtils
operator|.
name|randomVersionBetween
argument_list|(
name|random
argument_list|()
argument_list|,
name|VersionUtils
operator|.
name|getPreviousVersion
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|got
operator|.
name|onOrAfter
argument_list|(
name|VersionUtils
operator|.
name|getPreviousVersion
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|got
operator|.
name|onOrBefore
argument_list|(
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|)
expr_stmt|;
comment|// range of one
name|got
operator|=
name|VersionUtils
operator|.
name|randomVersionBetween
argument_list|(
name|random
argument_list|()
argument_list|,
name|VersionUtils
operator|.
name|getFirstVersion
argument_list|()
argument_list|,
name|VersionUtils
operator|.
name|getFirstVersion
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|got
argument_list|,
name|VersionUtils
operator|.
name|getFirstVersion
argument_list|()
argument_list|)
expr_stmt|;
name|got
operator|=
name|VersionUtils
operator|.
name|randomVersionBetween
argument_list|(
name|random
argument_list|()
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|got
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
expr_stmt|;
name|got
operator|=
name|VersionUtils
operator|.
name|randomVersionBetween
argument_list|(
name|random
argument_list|()
argument_list|,
name|Version
operator|.
name|V_5_0_0_alpha1
argument_list|,
name|Version
operator|.
name|V_5_0_0_alpha1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|got
argument_list|,
name|Version
operator|.
name|V_5_0_0_alpha1
argument_list|)
expr_stmt|;
comment|// implicit range of one
name|got
operator|=
name|VersionUtils
operator|.
name|randomVersionBetween
argument_list|(
name|random
argument_list|()
argument_list|,
literal|null
argument_list|,
name|VersionUtils
operator|.
name|getFirstVersion
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|got
argument_list|,
name|VersionUtils
operator|.
name|getFirstVersion
argument_list|()
argument_list|)
expr_stmt|;
name|got
operator|=
name|VersionUtils
operator|.
name|randomVersionBetween
argument_list|(
name|random
argument_list|()
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|got
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

