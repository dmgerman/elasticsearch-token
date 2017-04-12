begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.metadata
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
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
name|settings
operator|.
name|Settings
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
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_class
DECL|class|ClusterNameExpressionResolverTests
specifier|public
class|class
name|ClusterNameExpressionResolverTests
extends|extends
name|ESTestCase
block|{
DECL|field|clusterNameResolver
specifier|private
name|ClusterNameExpressionResolver
name|clusterNameResolver
init|=
operator|new
name|ClusterNameExpressionResolver
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
DECL|field|remoteClusters
specifier|private
specifier|static
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|remoteClusters
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
static|static
block|{
name|remoteClusters
operator|.
name|add
argument_list|(
literal|"cluster1"
argument_list|)
expr_stmt|;
name|remoteClusters
operator|.
name|add
argument_list|(
literal|"cluster2"
argument_list|)
expr_stmt|;
name|remoteClusters
operator|.
name|add
argument_list|(
literal|"totallyDifferent"
argument_list|)
expr_stmt|;
block|}
DECL|method|testExactMatch
specifier|public
name|void
name|testExactMatch
parameter_list|()
block|{
name|List
argument_list|<
name|String
argument_list|>
name|clusters
init|=
name|clusterNameResolver
operator|.
name|resolveClusterNames
argument_list|(
name|remoteClusters
argument_list|,
literal|"totallyDifferent"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"totallyDifferent"
argument_list|)
argument_list|)
argument_list|,
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|clusters
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testNoWildCardNoMatch
specifier|public
name|void
name|testNoWildCardNoMatch
parameter_list|()
block|{
name|List
argument_list|<
name|String
argument_list|>
name|clusters
init|=
name|clusterNameResolver
operator|.
name|resolveClusterNames
argument_list|(
name|remoteClusters
argument_list|,
literal|"totallyDifferent2"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|clusters
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testWildCardNoMatch
specifier|public
name|void
name|testWildCardNoMatch
parameter_list|()
block|{
name|List
argument_list|<
name|String
argument_list|>
name|clusters
init|=
name|clusterNameResolver
operator|.
name|resolveClusterNames
argument_list|(
name|remoteClusters
argument_list|,
literal|"totally*2"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|clusters
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testSimpleWildCard
specifier|public
name|void
name|testSimpleWildCard
parameter_list|()
block|{
name|List
argument_list|<
name|String
argument_list|>
name|clusters
init|=
name|clusterNameResolver
operator|.
name|resolveClusterNames
argument_list|(
name|remoteClusters
argument_list|,
literal|"*"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"cluster1"
argument_list|,
literal|"cluster2"
argument_list|,
literal|"totallyDifferent"
argument_list|)
argument_list|)
argument_list|,
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|clusters
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSuffixWildCard
specifier|public
name|void
name|testSuffixWildCard
parameter_list|()
block|{
name|List
argument_list|<
name|String
argument_list|>
name|clusters
init|=
name|clusterNameResolver
operator|.
name|resolveClusterNames
argument_list|(
name|remoteClusters
argument_list|,
literal|"cluster*"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"cluster1"
argument_list|,
literal|"cluster2"
argument_list|)
argument_list|)
argument_list|,
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|clusters
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testPrefixWildCard
specifier|public
name|void
name|testPrefixWildCard
parameter_list|()
block|{
name|List
argument_list|<
name|String
argument_list|>
name|clusters
init|=
name|clusterNameResolver
operator|.
name|resolveClusterNames
argument_list|(
name|remoteClusters
argument_list|,
literal|"*Different"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"totallyDifferent"
argument_list|)
argument_list|)
argument_list|,
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|clusters
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testMiddleWildCard
specifier|public
name|void
name|testMiddleWildCard
parameter_list|()
block|{
name|List
argument_list|<
name|String
argument_list|>
name|clusters
init|=
name|clusterNameResolver
operator|.
name|resolveClusterNames
argument_list|(
name|remoteClusters
argument_list|,
literal|"clu*1"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"cluster1"
argument_list|)
argument_list|)
argument_list|,
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|clusters
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

