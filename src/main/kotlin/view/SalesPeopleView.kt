package view

import app.*
import com.github.thomasnield.rxkotlinfx.*
import domain.SalesPerson
import domain.persistence.Persistence
import io.reactivex.Maybe
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.rxkotlin.toObservable
import javafx.beans.binding.Binding
import javafx.geometry.Orientation
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableView
import javafx.scene.paint.Color
import javafx.scene.text.Text
import org.controlsfx.glyphfont.FontAwesome
import org.controlsfx.glyphfont.GlyphFontRegistry
import tornadofx.*

class SalesPeopleView: View() {
    private val controller: ApplicationController by inject()
    private var table: TableView<SalesPerson> by singleAssign()

    private val fontAwesome = GlyphFontRegistry.font("FontAwesome")
    private val saveGlyph = fontAwesome.create(FontAwesome.Glyph.SAVE)
    private val refreshGlyph = fontAwesome.create(FontAwesome.Glyph.UNDO).color(Color.PURPLE)
    private val addGlyph = fontAwesome.create(FontAwesome.Glyph.PLUS).color(Color.BLUE)
    private val removeGlyph = fontAwesome.create(FontAwesome.Glyph.TIMES).color(Color.RED)

    private val db: Persistence by di()

    override val root = borderpane {


        left = toolbar {
            orientation = Orientation.VERTICAL

            //save button
            button("",saveGlyph) {
                useMaxWidth = true
                actionEvents()
                    .map { Unit }
                    .subscribe(controller.saveAssignments)
            }

            //refresh button
            button("",refreshGlyph) {
                useMaxWidth = true
                actionEvents()
                    .map { Unit }
                    .subscribe(controller.refreshSalesPeople)
            }

            //add button
            button("", addGlyph) {
                tooltip("Create a new Sales Person")
                useMaxWidth = true
                actionEvents()
                    .map { Unit }
                    .subscribe(controller.createNewSalesPerson)
            }

            //remove customer button
            button("",removeGlyph) {
                tooltip("Remove selected Customers")
                useMaxWidth = true
                actionEvents()
                    .flatMapSingle {
                        table.selectionModel.selectedItems
                            .mapNotNull { it.id }
                            .toObservable()
                            .toSet()
                    }
                    .subscribe(controller.deleteSalesPerson)
            }
        }

        top = label("SALES PEOPLE").addClass(Styles.heading)

        center = tableview<SalesPerson> {
            table = this

            readonlyColumn("ID",SalesPerson::id)
            readonlyColumn("First Name",SalesPerson::firstName)
            readonlyColumn("Last Name",SalesPerson::lastName)

            column("Assigned Clients", SalesPerson::customerAssignmentsConcat).cellFormat { (newList, originalList) ->
                graphic = textflow {
                    val iter = newList.iterator()
                    while(iter.hasNext()) {
                        val newValue = iter.next()
                        text(newValue.toString()) {
                            if (!originalList.contains(newValue)) {
                                fill = Color.RED
                            }
                        }
                        if (iter.hasNext()) {
                            text("|")
                        }
                    }
                }

            }


            selectionModel.selectionMode = SelectionMode.MULTIPLE

            // --- broadcast selections
            selectionModel.selectedItems.onChangedObservable()
                .map { it.asSequence().filterNotNull().toSet() }
                .subscribe(controller.selectedSalesPeople)

            // handle search requests
            controller.searchCustomerUsages.subscribe { ids ->
                moveToTopWhere { it.customerAssignments.any { it in ids } }
                requestFocus()
            }

            //handle adds
            controller.applyCustomers.subscribe { ids ->
                selectionModel.selectedItems.asSequence().filterNotNull().forEach {
                    it.customerAssignments.addIfAbsent(*ids.toTypedArray())
                }
            }

            //handle removals
            controller.removeCustomerUsages.subscribe { ids ->
                selectionModel.selectedItems.asSequence().filterNotNull().forEach {
                    it.customerAssignments.removeAll(ids)
                }
            }

            controller.handleAssignments(items)

            //handle refresh events and import data
            controller.refreshSalesPeople
                .doOnNext { items.forEach { it.dispose() } } //important to kill subscriptions on each SalesPerson
                .startWith(Unit)
                .flatMapSingle {
                    db.listAllSalesPersons().toList()
                }.subscribe { items.setAll(it) }

            //handle move up and move down requests
            controller.moveCustomerUp
                .map { it to selectedItem?.customerAssignments }
                .filter { it.second != null }
                .subscribe { it.second!!.moveUp(it.first) }

            //handle move up and move down requests
            controller.moveCustomerDown
                .map { it to selectedItem?.customerAssignments }
                .filter { it.second != null }
                .subscribe { it.second!!.moveDown(it.first) }
        }
    }
    init {
        //when customers are deleted, remove their usages
        controller.deletedCustomers.flatMap { deleteIds ->
            table.items.toObservable().doOnNext { it.customerAssignments.removeAll(deleteIds) }
        }.subscribe()

        //handle new Sales Person request
        controller.createNewSalesPerson
            .flatMap {
                NewSalesPersonDialog()
                    .toMaybe()
                    .toObservable()
                    .flatMap { it.toObservable() }
                    .flatMapMaybe {
                        if(it.id != null) {
                            db.loadSalesPerson(it.id)
                        } else {
                            Maybe.empty()
                        }
                    }
            }
            .subscribe {
                table.selectionModel.clearSelection()
                table.items.add(it)
                table.selectionModel.select(it)
                table.requestFocus()
            }

        //handle sales person deletions
        controller.deleteSalesPerson
            .flatMapSingle {
                table.currentSelections
                    .toList()
                    .flatMap { deleteItems ->
                        Alert(Alert.AlertType.WARNING, "Are you sure you want to delete these ${deleteItems.size} sales people?", ButtonType.YES, ButtonType.NO).toMaybe()
                            .filter { it == ButtonType.YES }
                            .flatMapObservable {  deleteItems.toObservable() }
                            .flatMapSingle {
                                if (it.id != null) {
                                    db.deleteSalesPerson(it.id)
                                } else {
                                    Maybe.empty<Int>().toSingle()
                                }

                            }
                            .toSet()
                    }
            }.subscribe { deletedIds ->
                table.items.deleteWhere { it.id in deletedIds }
            }
    }
}
