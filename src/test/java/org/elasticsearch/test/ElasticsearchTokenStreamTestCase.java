begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
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
name|annotations
operator|.
name|Listeners
import|;
end_import

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|annotations
operator|.
name|ThreadLeakFilters
import|;
end_import

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|annotations
operator|.
name|ThreadLeakScope
import|;
end_import

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|annotations
operator|.
name|ThreadLeakScope
operator|.
name|Scope
import|;
end_import

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|annotations
operator|.
name|TimeoutSuite
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
name|analysis
operator|.
name|BaseTokenStreamTestCase
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
name|LuceneTestCase
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
name|TimeUnits
import|;
end_import

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
name|junit
operator|.
name|listeners
operator|.
name|ReproduceInfoPrinter
import|;
end_import

begin_class
annotation|@
name|Listeners
argument_list|(
block|{
name|ReproduceInfoPrinter
operator|.
name|class
block|}
argument_list|)
annotation|@
name|ThreadLeakFilters
argument_list|(
name|defaultFilters
operator|=
literal|true
argument_list|,
name|filters
operator|=
block|{
name|ElasticsearchThreadFilter
operator|.
name|class
block|}
argument_list|)
annotation|@
name|ThreadLeakScope
argument_list|(
name|Scope
operator|.
name|NONE
argument_list|)
annotation|@
name|TimeoutSuite
argument_list|(
name|millis
operator|=
name|TimeUnits
operator|.
name|HOUR
argument_list|)
annotation|@
name|LuceneTestCase
operator|.
name|SuppressSysoutChecks
argument_list|(
name|bugUrl
operator|=
literal|"we log a lot on purpose"
argument_list|)
comment|/**  * Basic test case for token streams. the assertion methods in this class will  * run basic checks to enforce correct behavior of the token streams.  */
DECL|class|ElasticsearchTokenStreamTestCase
specifier|public
specifier|abstract
class|class
name|ElasticsearchTokenStreamTestCase
extends|extends
name|BaseTokenStreamTestCase
block|{
DECL|method|randomVersion
specifier|public
specifier|static
name|Version
name|randomVersion
parameter_list|()
block|{
return|return
name|ElasticsearchTestCase
operator|.
name|randomVersion
argument_list|(
name|random
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

