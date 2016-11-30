begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to You under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
DECL|package|org.apache.lucene.analysis.synonym
package|package
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|analysis
operator|.
name|synonym
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
name|analysis
operator|.
name|TokenFilter
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
name|TokenStream
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
name|tokenattributes
operator|.
name|CharTermAttribute
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
name|tokenattributes
operator|.
name|FlagsAttribute
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
name|tokenattributes
operator|.
name|OffsetAttribute
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
name|tokenattributes
operator|.
name|PositionIncrementAttribute
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
name|tokenattributes
operator|.
name|PositionLengthAttribute
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
name|tokenattributes
operator|.
name|TypeAttribute
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
name|store
operator|.
name|ByteArrayDataInput
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
name|AttributeSource
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
name|BytesRef
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
name|CharsRefBuilder
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
name|RollingBuffer
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
name|fst
operator|.
name|FST
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedList
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

begin_comment
comment|// TODO: maybe we should resolve token -> wordID then run
end_comment

begin_comment
comment|// FST on wordIDs, for better perf?
end_comment

begin_comment
comment|// TODO: a more efficient approach would be Aho/Corasick's
end_comment

begin_comment
comment|// algorithm
end_comment

begin_comment
comment|// http://en.wikipedia.org/wiki/Aho%E2%80%93Corasick_string_matching_algorithm
end_comment

begin_comment
comment|// It improves over the current approach here
end_comment

begin_comment
comment|// because it does not fully re-start matching at every
end_comment

begin_comment
comment|// token.  For example if one pattern is "a b c x"
end_comment

begin_comment
comment|// and another is "b c d" and the input is "a b c d", on
end_comment

begin_comment
comment|// trying to parse "a b c x" but failing when you got to x,
end_comment

begin_comment
comment|// rather than starting over again your really should
end_comment

begin_comment
comment|// immediately recognize that "b c d" matches at the next
end_comment

begin_comment
comment|// input.  I suspect this won't matter that much in
end_comment

begin_comment
comment|// practice, but it's possible on some set of synonyms it
end_comment

begin_comment
comment|// will.  We'd have to modify Aho/Corasick to enforce our
end_comment

begin_comment
comment|// conflict resolving (eg greedy matching) because that algo
end_comment

begin_comment
comment|// finds all matches.  This really amounts to adding a .*
end_comment

begin_comment
comment|// closure to the FST and then determinizing it.
end_comment

begin_comment
comment|//
end_comment

begin_comment
comment|// Another possible solution is described at http://www.cis.uni-muenchen.de/people/Schulz/Pub/dictle5.ps
end_comment

begin_comment
comment|/**  * Applies single- or multi-token synonyms from a {@link SynonymMap}  * to an incoming {@link TokenStream}, producing a fully correct graph  * output.  This is a replacement for {@link SynonymFilter}, which produces  * incorrect graphs for multi-token synonyms.  *  *<b>NOTE</b>: this cannot consume an incoming graph; results will  * be undefined.  */
end_comment

begin_class
DECL|class|SynonymGraphFilter
specifier|public
specifier|final
class|class
name|SynonymGraphFilter
extends|extends
name|TokenFilter
block|{
DECL|field|TYPE_SYNONYM
specifier|public
specifier|static
specifier|final
name|String
name|TYPE_SYNONYM
init|=
literal|"SYNONYM"
decl_stmt|;
DECL|field|GRAPH_FLAG
specifier|public
specifier|static
specifier|final
name|int
name|GRAPH_FLAG
init|=
literal|8
decl_stmt|;
DECL|field|termAtt
specifier|private
specifier|final
name|CharTermAttribute
name|termAtt
init|=
name|addAttribute
argument_list|(
name|CharTermAttribute
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|posIncrAtt
specifier|private
specifier|final
name|PositionIncrementAttribute
name|posIncrAtt
init|=
name|addAttribute
argument_list|(
name|PositionIncrementAttribute
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|posLenAtt
specifier|private
specifier|final
name|PositionLengthAttribute
name|posLenAtt
init|=
name|addAttribute
argument_list|(
name|PositionLengthAttribute
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|flagsAtt
specifier|private
specifier|final
name|FlagsAttribute
name|flagsAtt
init|=
name|addAttribute
argument_list|(
name|FlagsAttribute
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|typeAtt
specifier|private
specifier|final
name|TypeAttribute
name|typeAtt
init|=
name|addAttribute
argument_list|(
name|TypeAttribute
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|offsetAtt
specifier|private
specifier|final
name|OffsetAttribute
name|offsetAtt
init|=
name|addAttribute
argument_list|(
name|OffsetAttribute
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|synonyms
specifier|private
specifier|final
name|SynonymMap
name|synonyms
decl_stmt|;
DECL|field|ignoreCase
specifier|private
specifier|final
name|boolean
name|ignoreCase
decl_stmt|;
DECL|field|fst
specifier|private
specifier|final
name|FST
argument_list|<
name|BytesRef
argument_list|>
name|fst
decl_stmt|;
DECL|field|fstReader
specifier|private
specifier|final
name|FST
operator|.
name|BytesReader
name|fstReader
decl_stmt|;
DECL|field|scratchArc
specifier|private
specifier|final
name|FST
operator|.
name|Arc
argument_list|<
name|BytesRef
argument_list|>
name|scratchArc
decl_stmt|;
DECL|field|bytesReader
specifier|private
specifier|final
name|ByteArrayDataInput
name|bytesReader
init|=
operator|new
name|ByteArrayDataInput
argument_list|()
decl_stmt|;
DECL|field|scratchBytes
specifier|private
specifier|final
name|BytesRef
name|scratchBytes
init|=
operator|new
name|BytesRef
argument_list|()
decl_stmt|;
DECL|field|scratchChars
specifier|private
specifier|final
name|CharsRefBuilder
name|scratchChars
init|=
operator|new
name|CharsRefBuilder
argument_list|()
decl_stmt|;
DECL|field|outputBuffer
specifier|private
specifier|final
name|LinkedList
argument_list|<
name|BufferedOutputToken
argument_list|>
name|outputBuffer
init|=
operator|new
name|LinkedList
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|nextNodeOut
specifier|private
name|int
name|nextNodeOut
decl_stmt|;
DECL|field|lastNodeOut
specifier|private
name|int
name|lastNodeOut
decl_stmt|;
DECL|field|maxLookaheadUsed
specifier|private
name|int
name|maxLookaheadUsed
decl_stmt|;
comment|// For testing:
DECL|field|captureCount
specifier|private
name|int
name|captureCount
decl_stmt|;
DECL|field|liveToken
specifier|private
name|boolean
name|liveToken
decl_stmt|;
comment|// Start/end offset of the current match:
DECL|field|matchStartOffset
specifier|private
name|int
name|matchStartOffset
decl_stmt|;
DECL|field|matchEndOffset
specifier|private
name|int
name|matchEndOffset
decl_stmt|;
comment|// True once the input TokenStream is exhausted:
DECL|field|finished
specifier|private
name|boolean
name|finished
decl_stmt|;
DECL|field|lookaheadNextRead
specifier|private
name|int
name|lookaheadNextRead
decl_stmt|;
DECL|field|lookaheadNextWrite
specifier|private
name|int
name|lookaheadNextWrite
decl_stmt|;
DECL|field|lookahead
specifier|private
name|RollingBuffer
argument_list|<
name|BufferedInputToken
argument_list|>
name|lookahead
init|=
operator|new
name|RollingBuffer
argument_list|<
name|BufferedInputToken
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|BufferedInputToken
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|BufferedInputToken
argument_list|()
return|;
block|}
block|}
decl_stmt|;
DECL|class|BufferedInputToken
specifier|static
class|class
name|BufferedInputToken
implements|implements
name|RollingBuffer
operator|.
name|Resettable
block|{
DECL|field|term
specifier|final
name|CharsRefBuilder
name|term
init|=
operator|new
name|CharsRefBuilder
argument_list|()
decl_stmt|;
DECL|field|state
name|AttributeSource
operator|.
name|State
name|state
decl_stmt|;
DECL|field|startOffset
name|int
name|startOffset
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|endOffset
name|int
name|endOffset
init|=
operator|-
literal|1
decl_stmt|;
annotation|@
name|Override
DECL|method|reset
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|state
operator|=
literal|null
expr_stmt|;
name|term
operator|.
name|clear
argument_list|()
expr_stmt|;
comment|// Intentionally invalid to ferret out bugs:
name|startOffset
operator|=
operator|-
literal|1
expr_stmt|;
name|endOffset
operator|=
operator|-
literal|1
expr_stmt|;
block|}
block|}
DECL|class|BufferedOutputToken
specifier|static
class|class
name|BufferedOutputToken
block|{
DECL|field|term
specifier|final
name|String
name|term
decl_stmt|;
comment|// Non-null if this was an incoming token:
DECL|field|state
specifier|final
name|State
name|state
decl_stmt|;
DECL|field|startNode
specifier|final
name|int
name|startNode
decl_stmt|;
DECL|field|endNode
specifier|final
name|int
name|endNode
decl_stmt|;
DECL|method|BufferedOutputToken
specifier|public
name|BufferedOutputToken
parameter_list|(
name|State
name|state
parameter_list|,
name|String
name|term
parameter_list|,
name|int
name|startNode
parameter_list|,
name|int
name|endNode
parameter_list|)
block|{
name|this
operator|.
name|state
operator|=
name|state
expr_stmt|;
name|this
operator|.
name|term
operator|=
name|term
expr_stmt|;
name|this
operator|.
name|startNode
operator|=
name|startNode
expr_stmt|;
name|this
operator|.
name|endNode
operator|=
name|endNode
expr_stmt|;
block|}
block|}
DECL|method|SynonymGraphFilter
specifier|public
name|SynonymGraphFilter
parameter_list|(
name|TokenStream
name|input
parameter_list|,
name|SynonymMap
name|synonyms
parameter_list|,
name|boolean
name|ignoreCase
parameter_list|)
block|{
name|super
argument_list|(
name|input
argument_list|)
expr_stmt|;
name|this
operator|.
name|synonyms
operator|=
name|synonyms
expr_stmt|;
name|this
operator|.
name|fst
operator|=
name|synonyms
operator|.
name|fst
expr_stmt|;
if|if
condition|(
name|fst
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"fst must be non-null"
argument_list|)
throw|;
block|}
name|this
operator|.
name|fstReader
operator|=
name|fst
operator|.
name|getBytesReader
argument_list|()
expr_stmt|;
name|scratchArc
operator|=
operator|new
name|FST
operator|.
name|Arc
argument_list|<>
argument_list|()
expr_stmt|;
name|this
operator|.
name|ignoreCase
operator|=
name|ignoreCase
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|incrementToken
specifier|public
name|boolean
name|incrementToken
parameter_list|()
throws|throws
name|IOException
block|{
comment|//System.out.println("\nS: incrToken lastNodeOut=" + lastNodeOut + " nextNodeOut=" + nextNodeOut);
assert|assert
name|lastNodeOut
operator|<=
name|nextNodeOut
assert|;
if|if
condition|(
name|outputBuffer
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
condition|)
block|{
comment|// We still have pending outputs from a prior synonym match:
name|releaseBufferedToken
argument_list|()
expr_stmt|;
comment|//System.out.println("  syn: ret buffered=" + this);
assert|assert
name|liveToken
operator|==
literal|false
assert|;
return|return
literal|true
return|;
block|}
comment|// Try to parse a new synonym match at the current token:
if|if
condition|(
name|parse
argument_list|()
condition|)
block|{
comment|// A new match was found:
name|releaseBufferedToken
argument_list|()
expr_stmt|;
comment|//System.out.println("  syn: after parse, ret buffered=" + this);
assert|assert
name|liveToken
operator|==
literal|false
assert|;
return|return
literal|true
return|;
block|}
if|if
condition|(
name|lookaheadNextRead
operator|==
name|lookaheadNextWrite
condition|)
block|{
comment|// Fast path: parse pulled one token, but it didn't match
comment|// the start for any synonym, so we now return it "live" w/o having
comment|// cloned all of its atts:
if|if
condition|(
name|finished
condition|)
block|{
comment|//System.out.println("  syn: ret END");
return|return
literal|false
return|;
block|}
assert|assert
name|liveToken
assert|;
name|liveToken
operator|=
literal|false
expr_stmt|;
comment|// NOTE: no need to change posInc since it's relative, i.e. whatever
comment|// node our output is upto will just increase by the incoming posInc.
comment|// We also don't need to change posLen, but only because we cannot
comment|// consume a graph, so the incoming token can never span a future
comment|// synonym match.
block|}
else|else
block|{
comment|// We still have buffered lookahead tokens from a previous
comment|// parse attempt that required lookahead; just replay them now:
comment|//System.out.println("  restore buffer");
assert|assert
name|lookaheadNextRead
operator|<
name|lookaheadNextWrite
operator|:
literal|"read="
operator|+
name|lookaheadNextRead
operator|+
literal|" write="
operator|+
name|lookaheadNextWrite
assert|;
name|BufferedInputToken
name|token
init|=
name|lookahead
operator|.
name|get
argument_list|(
name|lookaheadNextRead
argument_list|)
decl_stmt|;
name|lookaheadNextRead
operator|++
expr_stmt|;
name|restoreState
argument_list|(
name|token
operator|.
name|state
argument_list|)
expr_stmt|;
name|lookahead
operator|.
name|freeBefore
argument_list|(
name|lookaheadNextRead
argument_list|)
expr_stmt|;
comment|//System.out.println("  after restore offset=" + offsetAtt.startOffset() + "-" + offsetAtt.endOffset());
assert|assert
name|liveToken
operator|==
literal|false
assert|;
block|}
name|lastNodeOut
operator|+=
name|posIncrAtt
operator|.
name|getPositionIncrement
argument_list|()
expr_stmt|;
name|nextNodeOut
operator|=
name|lastNodeOut
operator|+
name|posLenAtt
operator|.
name|getPositionLength
argument_list|()
expr_stmt|;
comment|//System.out.println("  syn: ret lookahead=" + this);
return|return
literal|true
return|;
block|}
DECL|method|releaseBufferedToken
specifier|private
name|void
name|releaseBufferedToken
parameter_list|()
throws|throws
name|IOException
block|{
comment|//System.out.println("  releaseBufferedToken");
name|BufferedOutputToken
name|token
init|=
name|outputBuffer
operator|.
name|pollFirst
argument_list|()
decl_stmt|;
if|if
condition|(
name|token
operator|.
name|state
operator|!=
literal|null
condition|)
block|{
comment|// This is an original input token (keepOrig=true case):
comment|//System.out.println("    hasState");
name|restoreState
argument_list|(
name|token
operator|.
name|state
argument_list|)
expr_stmt|;
comment|//System.out.println("    startOffset=" + offsetAtt.startOffset() + " endOffset=" + offsetAtt.endOffset());
block|}
else|else
block|{
name|clearAttributes
argument_list|()
expr_stmt|;
comment|//System.out.println("    no state");
name|termAtt
operator|.
name|append
argument_list|(
name|token
operator|.
name|term
argument_list|)
expr_stmt|;
comment|// We better have a match already:
assert|assert
name|matchStartOffset
operator|!=
operator|-
literal|1
assert|;
name|offsetAtt
operator|.
name|setOffset
argument_list|(
name|matchStartOffset
argument_list|,
name|matchEndOffset
argument_list|)
expr_stmt|;
comment|//System.out.println("    startOffset=" + matchStartOffset + " endOffset=" + matchEndOffset);
name|typeAtt
operator|.
name|setType
argument_list|(
name|TYPE_SYNONYM
argument_list|)
expr_stmt|;
block|}
comment|//System.out.println("    lastNodeOut=" + lastNodeOut);
comment|//System.out.println("    term=" + termAtt);
name|posIncrAtt
operator|.
name|setPositionIncrement
argument_list|(
name|token
operator|.
name|startNode
operator|-
name|lastNodeOut
argument_list|)
expr_stmt|;
name|lastNodeOut
operator|=
name|token
operator|.
name|startNode
expr_stmt|;
name|posLenAtt
operator|.
name|setPositionLength
argument_list|(
name|token
operator|.
name|endNode
operator|-
name|token
operator|.
name|startNode
argument_list|)
expr_stmt|;
name|flagsAtt
operator|.
name|setFlags
argument_list|(
name|flagsAtt
operator|.
name|getFlags
argument_list|()
operator||
name|GRAPH_FLAG
argument_list|)
expr_stmt|;
comment|// set the graph flag
block|}
comment|/**      * Scans the next input token(s) to see if a synonym matches.  Returns true      * if a match was found.      */
DECL|method|parse
specifier|private
name|boolean
name|parse
parameter_list|()
throws|throws
name|IOException
block|{
comment|// System.out.println(Thread.currentThread().getName() + ": S: parse: " + System.identityHashCode(this));
comment|// Holds the longest match we've seen so far:
name|BytesRef
name|matchOutput
init|=
literal|null
decl_stmt|;
name|int
name|matchInputLength
init|=
literal|0
decl_stmt|;
name|BytesRef
name|pendingOutput
init|=
name|fst
operator|.
name|outputs
operator|.
name|getNoOutput
argument_list|()
decl_stmt|;
name|fst
operator|.
name|getFirstArc
argument_list|(
name|scratchArc
argument_list|)
expr_stmt|;
assert|assert
name|scratchArc
operator|.
name|output
operator|==
name|fst
operator|.
name|outputs
operator|.
name|getNoOutput
argument_list|()
assert|;
comment|// How many tokens in the current match
name|int
name|matchLength
init|=
literal|0
decl_stmt|;
name|boolean
name|doFinalCapture
init|=
literal|false
decl_stmt|;
name|int
name|lookaheadUpto
init|=
name|lookaheadNextRead
decl_stmt|;
name|matchStartOffset
operator|=
operator|-
literal|1
expr_stmt|;
name|byToken
label|:
while|while
condition|(
literal|true
condition|)
block|{
comment|//System.out.println("  cycle lookaheadUpto=" + lookaheadUpto + " maxPos=" + lookahead.getMaxPos());
comment|// Pull next token's chars:
specifier|final
name|char
index|[]
name|buffer
decl_stmt|;
specifier|final
name|int
name|bufferLen
decl_stmt|;
specifier|final
name|int
name|inputEndOffset
decl_stmt|;
if|if
condition|(
name|lookaheadUpto
operator|<=
name|lookahead
operator|.
name|getMaxPos
argument_list|()
condition|)
block|{
comment|// Still in our lookahead buffer
name|BufferedInputToken
name|token
init|=
name|lookahead
operator|.
name|get
argument_list|(
name|lookaheadUpto
argument_list|)
decl_stmt|;
name|lookaheadUpto
operator|++
expr_stmt|;
name|buffer
operator|=
name|token
operator|.
name|term
operator|.
name|chars
argument_list|()
expr_stmt|;
name|bufferLen
operator|=
name|token
operator|.
name|term
operator|.
name|length
argument_list|()
expr_stmt|;
name|inputEndOffset
operator|=
name|token
operator|.
name|endOffset
expr_stmt|;
comment|//System.out.println("    use buffer now max=" + lookahead.getMaxPos());
if|if
condition|(
name|matchStartOffset
operator|==
operator|-
literal|1
condition|)
block|{
name|matchStartOffset
operator|=
name|token
operator|.
name|startOffset
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// We used up our lookahead buffer of input tokens
comment|// -- pull next real input token:
assert|assert
name|finished
operator|||
name|liveToken
operator|==
literal|false
assert|;
if|if
condition|(
name|finished
condition|)
block|{
comment|//System.out.println("    break: finished");
break|break;
block|}
elseif|else
if|if
condition|(
name|input
operator|.
name|incrementToken
argument_list|()
condition|)
block|{
comment|//System.out.println("    input.incrToken");
name|liveToken
operator|=
literal|true
expr_stmt|;
name|buffer
operator|=
name|termAtt
operator|.
name|buffer
argument_list|()
expr_stmt|;
name|bufferLen
operator|=
name|termAtt
operator|.
name|length
argument_list|()
expr_stmt|;
if|if
condition|(
name|matchStartOffset
operator|==
operator|-
literal|1
condition|)
block|{
name|matchStartOffset
operator|=
name|offsetAtt
operator|.
name|startOffset
argument_list|()
expr_stmt|;
block|}
name|inputEndOffset
operator|=
name|offsetAtt
operator|.
name|endOffset
argument_list|()
expr_stmt|;
name|lookaheadUpto
operator|++
expr_stmt|;
block|}
else|else
block|{
comment|// No more input tokens
name|finished
operator|=
literal|true
expr_stmt|;
comment|//System.out.println("    break: now set finished");
break|break;
block|}
block|}
name|matchLength
operator|++
expr_stmt|;
comment|//System.out.println("    cycle term=" + new String(buffer, 0, bufferLen));
comment|// Run each char in this token through the FST:
name|int
name|bufUpto
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|bufUpto
operator|<
name|bufferLen
condition|)
block|{
specifier|final
name|int
name|codePoint
init|=
name|Character
operator|.
name|codePointAt
argument_list|(
name|buffer
argument_list|,
name|bufUpto
argument_list|,
name|bufferLen
argument_list|)
decl_stmt|;
if|if
condition|(
name|fst
operator|.
name|findTargetArc
argument_list|(
name|ignoreCase
condition|?
name|Character
operator|.
name|toLowerCase
argument_list|(
name|codePoint
argument_list|)
else|:
name|codePoint
argument_list|,
name|scratchArc
argument_list|,
name|scratchArc
argument_list|,
name|fstReader
argument_list|)
operator|==
literal|null
condition|)
block|{
break|break
name|byToken
break|;
block|}
comment|// Accum the output
name|pendingOutput
operator|=
name|fst
operator|.
name|outputs
operator|.
name|add
argument_list|(
name|pendingOutput
argument_list|,
name|scratchArc
operator|.
name|output
argument_list|)
expr_stmt|;
name|bufUpto
operator|+=
name|Character
operator|.
name|charCount
argument_list|(
name|codePoint
argument_list|)
expr_stmt|;
block|}
assert|assert
name|bufUpto
operator|==
name|bufferLen
assert|;
comment|// OK, entire token matched; now see if this is a final
comment|// state in the FST (a match):
if|if
condition|(
name|scratchArc
operator|.
name|isFinal
argument_list|()
condition|)
block|{
name|matchOutput
operator|=
name|fst
operator|.
name|outputs
operator|.
name|add
argument_list|(
name|pendingOutput
argument_list|,
name|scratchArc
operator|.
name|nextFinalOutput
argument_list|)
expr_stmt|;
name|matchInputLength
operator|=
name|matchLength
expr_stmt|;
name|matchEndOffset
operator|=
name|inputEndOffset
expr_stmt|;
comment|//System.out.println("    ** match");
block|}
comment|// See if the FST can continue matching (ie, needs to
comment|// see the next input token):
if|if
condition|(
name|fst
operator|.
name|findTargetArc
argument_list|(
name|SynonymMap
operator|.
name|WORD_SEPARATOR
argument_list|,
name|scratchArc
argument_list|,
name|scratchArc
argument_list|,
name|fstReader
argument_list|)
operator|==
literal|null
condition|)
block|{
comment|// No further rules can match here; we're done
comment|// searching for matching rules starting at the
comment|// current input position.
break|break;
block|}
else|else
block|{
comment|// More matching is possible -- accum the output (if
comment|// any) of the WORD_SEP arc:
name|pendingOutput
operator|=
name|fst
operator|.
name|outputs
operator|.
name|add
argument_list|(
name|pendingOutput
argument_list|,
name|scratchArc
operator|.
name|output
argument_list|)
expr_stmt|;
name|doFinalCapture
operator|=
literal|true
expr_stmt|;
if|if
condition|(
name|liveToken
condition|)
block|{
name|capture
argument_list|()
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|doFinalCapture
operator|&&
name|liveToken
operator|&&
name|finished
operator|==
literal|false
condition|)
block|{
comment|// Must capture the final token if we captured any prior tokens:
name|capture
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|matchOutput
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|liveToken
condition|)
block|{
comment|// Single input token synonym; we must buffer it now:
name|capture
argument_list|()
expr_stmt|;
block|}
comment|// There is a match!
name|bufferOutputTokens
argument_list|(
name|matchOutput
argument_list|,
name|matchInputLength
argument_list|)
expr_stmt|;
name|lookaheadNextRead
operator|+=
name|matchInputLength
expr_stmt|;
comment|//System.out.println("  precmatch; set lookaheadNextRead=" + lookaheadNextRead + " now max=" + lookahead.getMaxPos());
name|lookahead
operator|.
name|freeBefore
argument_list|(
name|lookaheadNextRead
argument_list|)
expr_stmt|;
comment|//System.out.println("  match; set lookaheadNextRead=" + lookaheadNextRead + " now max=" + lookahead.getMaxPos());
return|return
literal|true
return|;
block|}
else|else
block|{
comment|//System.out.println("  no match; lookaheadNextRead=" + lookaheadNextRead);
return|return
literal|false
return|;
block|}
comment|//System.out.println("  parse done inputSkipCount=" + inputSkipCount + " nextRead=" + nextRead + " nextWrite=" + nextWrite);
block|}
comment|/**      * Expands the output graph into the necessary tokens, adding      * synonyms as side paths parallel to the input tokens, and      * buffers them in the output token buffer.      */
DECL|method|bufferOutputTokens
specifier|private
name|void
name|bufferOutputTokens
parameter_list|(
name|BytesRef
name|bytes
parameter_list|,
name|int
name|matchInputLength
parameter_list|)
block|{
name|bytesReader
operator|.
name|reset
argument_list|(
name|bytes
operator|.
name|bytes
argument_list|,
name|bytes
operator|.
name|offset
argument_list|,
name|bytes
operator|.
name|length
argument_list|)
expr_stmt|;
specifier|final
name|int
name|code
init|=
name|bytesReader
operator|.
name|readVInt
argument_list|()
decl_stmt|;
specifier|final
name|boolean
name|keepOrig
init|=
operator|(
name|code
operator|&
literal|0x1
operator|)
operator|==
literal|0
decl_stmt|;
comment|//System.out.println("  buffer: keepOrig=" + keepOrig + " matchInputLength=" + matchInputLength);
comment|// How many nodes along all paths; we need this to assign the
comment|// node ID for the final end node where all paths merge back:
name|int
name|totalPathNodes
decl_stmt|;
if|if
condition|(
name|keepOrig
condition|)
block|{
assert|assert
name|matchInputLength
operator|>
literal|0
assert|;
name|totalPathNodes
operator|=
name|matchInputLength
operator|-
literal|1
expr_stmt|;
block|}
else|else
block|{
name|totalPathNodes
operator|=
literal|0
expr_stmt|;
block|}
comment|// How many synonyms we will insert over this match:
specifier|final
name|int
name|count
init|=
name|code
operator|>>>
literal|1
decl_stmt|;
comment|// TODO: we could encode this instead into the FST:
comment|// 1st pass: count how many new nodes we need
name|List
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|paths
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|outputIDX
init|=
literal|0
init|;
name|outputIDX
operator|<
name|count
condition|;
name|outputIDX
operator|++
control|)
block|{
name|int
name|wordID
init|=
name|bytesReader
operator|.
name|readVInt
argument_list|()
decl_stmt|;
name|synonyms
operator|.
name|words
operator|.
name|get
argument_list|(
name|wordID
argument_list|,
name|scratchBytes
argument_list|)
expr_stmt|;
name|scratchChars
operator|.
name|copyUTF8Bytes
argument_list|(
name|scratchBytes
argument_list|)
expr_stmt|;
name|int
name|lastStart
init|=
literal|0
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|path
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|paths
operator|.
name|add
argument_list|(
name|path
argument_list|)
expr_stmt|;
name|int
name|chEnd
init|=
name|scratchChars
operator|.
name|length
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|chUpto
init|=
literal|0
init|;
name|chUpto
operator|<=
name|chEnd
condition|;
name|chUpto
operator|++
control|)
block|{
if|if
condition|(
name|chUpto
operator|==
name|chEnd
operator|||
name|scratchChars
operator|.
name|charAt
argument_list|(
name|chUpto
argument_list|)
operator|==
name|SynonymMap
operator|.
name|WORD_SEPARATOR
condition|)
block|{
name|path
operator|.
name|add
argument_list|(
operator|new
name|String
argument_list|(
name|scratchChars
operator|.
name|chars
argument_list|()
argument_list|,
name|lastStart
argument_list|,
name|chUpto
operator|-
name|lastStart
argument_list|)
argument_list|)
expr_stmt|;
name|lastStart
operator|=
literal|1
operator|+
name|chUpto
expr_stmt|;
block|}
block|}
assert|assert
name|path
operator|.
name|size
argument_list|()
operator|>
literal|0
assert|;
name|totalPathNodes
operator|+=
name|path
operator|.
name|size
argument_list|()
operator|-
literal|1
expr_stmt|;
block|}
comment|//System.out.println("  totalPathNodes=" + totalPathNodes);
comment|// 2nd pass: buffer tokens for the graph fragment
comment|// NOTE: totalPathNodes will be 0 in the case where the matched
comment|// input is a single token and all outputs are also a single token
comment|// We "spawn" a side-path for each of the outputs for this matched
comment|// synonym, all ending back at this end node:
name|int
name|startNode
init|=
name|nextNodeOut
decl_stmt|;
name|int
name|endNode
init|=
name|startNode
operator|+
name|totalPathNodes
operator|+
literal|1
decl_stmt|;
comment|//System.out.println("  " + paths.size() + " new side-paths");
comment|// First, fanout all tokens departing start node for these new side paths:
name|int
name|newNodeCount
init|=
literal|0
decl_stmt|;
for|for
control|(
name|List
argument_list|<
name|String
argument_list|>
name|path
range|:
name|paths
control|)
block|{
name|int
name|pathEndNode
decl_stmt|;
comment|//System.out.println("    path size=" + path.size());
if|if
condition|(
name|path
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
comment|// Single token output, so there are no intermediate nodes:
name|pathEndNode
operator|=
name|endNode
expr_stmt|;
block|}
else|else
block|{
name|pathEndNode
operator|=
name|nextNodeOut
operator|+
name|newNodeCount
operator|+
literal|1
expr_stmt|;
name|newNodeCount
operator|+=
name|path
operator|.
name|size
argument_list|()
operator|-
literal|1
expr_stmt|;
block|}
name|outputBuffer
operator|.
name|add
argument_list|(
operator|new
name|BufferedOutputToken
argument_list|(
literal|null
argument_list|,
name|path
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|startNode
argument_list|,
name|pathEndNode
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// We must do the original tokens last, else the offsets "go backwards":
if|if
condition|(
name|keepOrig
condition|)
block|{
name|BufferedInputToken
name|token
init|=
name|lookahead
operator|.
name|get
argument_list|(
name|lookaheadNextRead
argument_list|)
decl_stmt|;
name|int
name|inputEndNode
decl_stmt|;
if|if
condition|(
name|matchInputLength
operator|==
literal|1
condition|)
block|{
comment|// Single token matched input, so there are no intermediate nodes:
name|inputEndNode
operator|=
name|endNode
expr_stmt|;
block|}
else|else
block|{
name|inputEndNode
operator|=
name|nextNodeOut
operator|+
name|newNodeCount
operator|+
literal|1
expr_stmt|;
block|}
comment|//System.out.println("    keepOrig first token: " + token.term);
name|outputBuffer
operator|.
name|add
argument_list|(
operator|new
name|BufferedOutputToken
argument_list|(
name|token
operator|.
name|state
argument_list|,
name|token
operator|.
name|term
operator|.
name|toString
argument_list|()
argument_list|,
name|startNode
argument_list|,
name|inputEndNode
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|nextNodeOut
operator|=
name|endNode
expr_stmt|;
comment|// Do full side-path for each syn output:
for|for
control|(
name|int
name|pathID
init|=
literal|0
init|;
name|pathID
operator|<
name|paths
operator|.
name|size
argument_list|()
condition|;
name|pathID
operator|++
control|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|path
init|=
name|paths
operator|.
name|get
argument_list|(
name|pathID
argument_list|)
decl_stmt|;
if|if
condition|(
name|path
operator|.
name|size
argument_list|()
operator|>
literal|1
condition|)
block|{
name|int
name|lastNode
init|=
name|outputBuffer
operator|.
name|get
argument_list|(
name|pathID
argument_list|)
operator|.
name|endNode
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|path
operator|.
name|size
argument_list|()
operator|-
literal|1
condition|;
name|i
operator|++
control|)
block|{
name|outputBuffer
operator|.
name|add
argument_list|(
operator|new
name|BufferedOutputToken
argument_list|(
literal|null
argument_list|,
name|path
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|lastNode
argument_list|,
name|lastNode
operator|+
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|lastNode
operator|++
expr_stmt|;
block|}
name|outputBuffer
operator|.
name|add
argument_list|(
operator|new
name|BufferedOutputToken
argument_list|(
literal|null
argument_list|,
name|path
operator|.
name|get
argument_list|(
name|path
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
argument_list|,
name|lastNode
argument_list|,
name|endNode
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|keepOrig
operator|&&
name|matchInputLength
operator|>
literal|1
condition|)
block|{
comment|// Do full "side path" with the original tokens:
name|int
name|lastNode
init|=
name|outputBuffer
operator|.
name|get
argument_list|(
name|paths
operator|.
name|size
argument_list|()
argument_list|)
operator|.
name|endNode
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|matchInputLength
operator|-
literal|1
condition|;
name|i
operator|++
control|)
block|{
name|BufferedInputToken
name|token
init|=
name|lookahead
operator|.
name|get
argument_list|(
name|lookaheadNextRead
operator|+
name|i
argument_list|)
decl_stmt|;
name|outputBuffer
operator|.
name|add
argument_list|(
operator|new
name|BufferedOutputToken
argument_list|(
name|token
operator|.
name|state
argument_list|,
name|token
operator|.
name|term
operator|.
name|toString
argument_list|()
argument_list|,
name|lastNode
argument_list|,
name|lastNode
operator|+
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|lastNode
operator|++
expr_stmt|;
block|}
name|BufferedInputToken
name|token
init|=
name|lookahead
operator|.
name|get
argument_list|(
name|lookaheadNextRead
operator|+
name|matchInputLength
operator|-
literal|1
argument_list|)
decl_stmt|;
name|outputBuffer
operator|.
name|add
argument_list|(
operator|new
name|BufferedOutputToken
argument_list|(
name|token
operator|.
name|state
argument_list|,
name|token
operator|.
name|term
operator|.
name|toString
argument_list|()
argument_list|,
name|lastNode
argument_list|,
name|endNode
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/*     System.out.println("  after buffer: " + outputBuffer.size() + " tokens:");     for(BufferedOutputToken token : outputBuffer) {       System.out.println("    tok: " + token.term + " startNode=" + token.startNode + " endNode=" + token.endNode);     }     */
block|}
comment|/**      * Buffers the current input token into lookahead buffer.      */
DECL|method|capture
specifier|private
name|void
name|capture
parameter_list|()
block|{
assert|assert
name|liveToken
assert|;
name|liveToken
operator|=
literal|false
expr_stmt|;
name|BufferedInputToken
name|token
init|=
name|lookahead
operator|.
name|get
argument_list|(
name|lookaheadNextWrite
argument_list|)
decl_stmt|;
name|lookaheadNextWrite
operator|++
expr_stmt|;
name|token
operator|.
name|state
operator|=
name|captureState
argument_list|()
expr_stmt|;
name|token
operator|.
name|startOffset
operator|=
name|offsetAtt
operator|.
name|startOffset
argument_list|()
expr_stmt|;
name|token
operator|.
name|endOffset
operator|=
name|offsetAtt
operator|.
name|endOffset
argument_list|()
expr_stmt|;
assert|assert
name|token
operator|.
name|term
operator|.
name|length
argument_list|()
operator|==
literal|0
assert|;
name|token
operator|.
name|term
operator|.
name|append
argument_list|(
name|termAtt
argument_list|)
expr_stmt|;
name|captureCount
operator|++
expr_stmt|;
name|maxLookaheadUsed
operator|=
name|Math
operator|.
name|max
argument_list|(
name|maxLookaheadUsed
argument_list|,
name|lookahead
operator|.
name|getBufferSize
argument_list|()
argument_list|)
expr_stmt|;
comment|//System.out.println("  maxLookaheadUsed=" + maxLookaheadUsed);
block|}
annotation|@
name|Override
DECL|method|reset
specifier|public
name|void
name|reset
parameter_list|()
throws|throws
name|IOException
block|{
name|super
operator|.
name|reset
argument_list|()
expr_stmt|;
name|lookahead
operator|.
name|reset
argument_list|()
expr_stmt|;
name|lookaheadNextWrite
operator|=
literal|0
expr_stmt|;
name|lookaheadNextRead
operator|=
literal|0
expr_stmt|;
name|captureCount
operator|=
literal|0
expr_stmt|;
name|lastNodeOut
operator|=
operator|-
literal|1
expr_stmt|;
name|nextNodeOut
operator|=
literal|0
expr_stmt|;
name|matchStartOffset
operator|=
operator|-
literal|1
expr_stmt|;
name|matchEndOffset
operator|=
operator|-
literal|1
expr_stmt|;
name|finished
operator|=
literal|false
expr_stmt|;
name|liveToken
operator|=
literal|false
expr_stmt|;
name|outputBuffer
operator|.
name|clear
argument_list|()
expr_stmt|;
name|maxLookaheadUsed
operator|=
literal|0
expr_stmt|;
comment|//System.out.println("S: reset");
block|}
comment|// for testing
DECL|method|getCaptureCount
name|int
name|getCaptureCount
parameter_list|()
block|{
return|return
name|captureCount
return|;
block|}
comment|// for testing
DECL|method|getMaxLookaheadUsed
name|int
name|getMaxLookaheadUsed
parameter_list|()
block|{
return|return
name|maxLookaheadUsed
return|;
block|}
block|}
end_class

end_unit

