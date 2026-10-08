package edu.curtin.oose.bikeApp;  // <-- you can modify this
import static edu.curtin.oose.bikeApp.mistletoe.Mistletoe.C;
import static edu.curtin.oose.bikeApp.mistletoe.Mistletoe.Fn;
import static edu.curtin.oose.bikeApp.mistletoe.Mistletoe.If;
import edu.curtin.oose.bikeApp.mistletoe.Mistletoe.M;
import static edu.curtin.oose.bikeApp.mistletoe.Mistletoe.MT;
import static edu.curtin.oose.bikeApp.mistletoe.Mistletoe.Note;
import static edu.curtin.oose.bikeApp.mistletoe.Mistletoe.V;
import static edu.curtin.oose.bikeApp.mistletoe.Mistletoe.While;

/**
 * DO NOT MODIFY THIS FILE (except as necessary to organise your packages). This is just a utility
 * to supply data to your Assignment 2 simulation app.
 *
 * You must create a new Scenario object, then call 'nextMessage()' periodically, which returns
 * the next message in a queue of randomly-generated messages.
 *
 * Note that some of the randomly-generated messages will be invalid!
 */

public class Scenario
{
    public String nextMessage()
    {
        var msg = MT(V("nextMessage").Call());
        return msg.isEmpty() ? null : (String)msg.get(0);
    }

    {
        MT(
            V("genLetters").Set(Fn(
                Note("Produces a string formed from random characters"),

                M.Arg.As("base", "range", "len"),
                While(M.Idx.Lt(V("len"))).Do(
                    V("base").Plus(M.Rand.Times(V("range")).Int())
                ).Chr().Join()
            )),

            V("makeEmail").Set(Fn(
                C(
                    V("genLetters").Call(C("a").Ord(), 26, M.Rand.Times(5).Plus(3).Int()),
                    "@",
                    V("genLetters").Call(C("a").Ord(), 26, M.Rand.Times(5).Plus(3).Int()),
                    ".",
                    V("genLetters").Call(C("a").Ord(), 26, 3)
                ).Join()
            )),

            Note("Various constants..."),

            V("DELIVERY_P").Set(0.1),
            V("DROP_OFF_P").Set(0.25),
            V("PURCHASE_ONLINE_P").Set(0.4),
            V("PURCHASE_IN_STORE_P").Set(0.4),
            V("PICK_UP_P").Set(0.25),
            V("SCREW_UP_P").Set(0.05),

            V("lastTime").Set(M.Time),
            V("messages").Set(),
            V("emails").Set(),

            V("nextMessage").SetGlobal(Fn(
                V("thisTime").Set(M.Time),

                V("t").Set(V("lastTime").Plus(0.999)),
                While(V("t").Lt(V("thisTime"))).Do(
                    V("messages").Append(
                        C(
                            While(M.Rand.Lt(V("DELIVERY_P")).Note("while a random number is < DELIVERY_P")).Do(
                                "DELIVERY"
                            ),
                            While(M.Rand.Lt(V("DROP_OFF_P"))).Do(
                                C(
                                    "DROP-OFF ",
                                    V("makeEmail").Call().With(
                                        V("emails").Append(M.It),
                                        M.It
                                    )
                                ).Join()
                            ),
                            While(M.Rand.Lt(V("PURCHASE_ONLINE_P"))).Do(
                                C(
                                    "PURCHASE-ONLINE ",
                                    V("makeEmail").Call().With(
                                        V("emails").Append(M.It),
                                        M.It
                                    )
                                ).Join()
                            ),
                            While(M.Rand.Lt(V("PURCHASE_IN_STORE_P"))).Do(
                                "PURCHASE-IN-STORE"
                            ),
                            While(V("emails").Len().Gt(0).And(M.Rand.Lt(V("PICK_UP_P")))).Do(
                                C("PICK-UP ").Cat(V("emails").RandChoose())
                            ),
                            While(M.Rand.Lt(V("SCREW_UP_P"))).Do(
                                C(
                                    V("genLetters").Call(C(" ").Ord(), 95, M.Rand.Times(30).Int()),
                                    If( V("emails").Len().Gt(0).And(M.Rand.Lt(0.5)) ).Then(
                                        " ",
                                        V("emails").RandChoose()
                                    )
                                ).Join()
                            )
                        ).RandShuffle()
                    ),
                    V("t").Set(M.Val.Plus(1.0))
                ),

                V("lastTime").Update(V("thisTime")),
                V("messages").Pop()
            ))
        );
    }
}
