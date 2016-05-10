begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.painless
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|painless
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
name|index
operator|.
name|IndexService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|CompiledScript
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|ScriptService
operator|.
name|ScriptType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|SearchScript
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
name|lookup
operator|.
name|SearchLookup
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
name|ESSingleNodeTestCase
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

begin_comment
comment|/**  * Test that needsScores() is reported correctly depending on whether _score is used  */
end_comment

begin_comment
comment|// TODO: can we test this better? this is a port of the ExpressionsTests method.
end_comment

begin_class
DECL|class|NeedsScoreTests
specifier|public
class|class
name|NeedsScoreTests
extends|extends
name|ESSingleNodeTestCase
block|{
DECL|method|testNeedsScores
specifier|public
name|void
name|testNeedsScores
parameter_list|()
block|{
name|IndexService
name|index
init|=
name|createIndex
argument_list|(
literal|"test"
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|,
literal|"type"
argument_list|,
literal|"d"
argument_list|,
literal|"type=double"
argument_list|)
decl_stmt|;
name|PainlessScriptEngineService
name|service
init|=
operator|new
name|PainlessScriptEngineService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
name|SearchLookup
name|lookup
init|=
operator|new
name|SearchLookup
argument_list|(
name|index
operator|.
name|mapperService
argument_list|()
argument_list|,
name|index
operator|.
name|fieldData
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|Object
name|compiled
init|=
name|service
operator|.
name|compile
argument_list|(
literal|"1.2"
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
decl_stmt|;
name|SearchScript
name|ss
init|=
name|service
operator|.
name|search
argument_list|(
operator|new
name|CompiledScript
argument_list|(
name|ScriptType
operator|.
name|INLINE
argument_list|,
literal|"randomName"
argument_list|,
literal|"painless"
argument_list|,
name|compiled
argument_list|)
argument_list|,
name|lookup
argument_list|,
name|Collections
operator|.
expr|<
name|String
argument_list|,
name|Object
operator|>
name|emptyMap
argument_list|()
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|ss
operator|.
name|needsScores
argument_list|()
argument_list|)
expr_stmt|;
name|compiled
operator|=
name|service
operator|.
name|compile
argument_list|(
literal|"input.doc['d'].value"
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
expr_stmt|;
name|ss
operator|=
name|service
operator|.
name|search
argument_list|(
operator|new
name|CompiledScript
argument_list|(
name|ScriptType
operator|.
name|INLINE
argument_list|,
literal|"randomName"
argument_list|,
literal|"painless"
argument_list|,
name|compiled
argument_list|)
argument_list|,
name|lookup
argument_list|,
name|Collections
operator|.
expr|<
name|String
argument_list|,
name|Object
operator|>
name|emptyMap
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|ss
operator|.
name|needsScores
argument_list|()
argument_list|)
expr_stmt|;
name|compiled
operator|=
name|service
operator|.
name|compile
argument_list|(
literal|"1/_score"
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
expr_stmt|;
name|ss
operator|=
name|service
operator|.
name|search
argument_list|(
operator|new
name|CompiledScript
argument_list|(
name|ScriptType
operator|.
name|INLINE
argument_list|,
literal|"randomName"
argument_list|,
literal|"painless"
argument_list|,
name|compiled
argument_list|)
argument_list|,
name|lookup
argument_list|,
name|Collections
operator|.
expr|<
name|String
argument_list|,
name|Object
operator|>
name|emptyMap
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|ss
operator|.
name|needsScores
argument_list|()
argument_list|)
expr_stmt|;
name|compiled
operator|=
name|service
operator|.
name|compile
argument_list|(
literal|"input.doc['d'].value * _score"
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
expr_stmt|;
name|ss
operator|=
name|service
operator|.
name|search
argument_list|(
operator|new
name|CompiledScript
argument_list|(
name|ScriptType
operator|.
name|INLINE
argument_list|,
literal|"randomName"
argument_list|,
literal|"painless"
argument_list|,
name|compiled
argument_list|)
argument_list|,
name|lookup
argument_list|,
name|Collections
operator|.
expr|<
name|String
argument_list|,
name|Object
operator|>
name|emptyMap
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|ss
operator|.
name|needsScores
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

